param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("start", "stop", "restart", "status", "metrics", "logs")]
    [string] $Command,

    [string] $Pod,

    [int] $Tail = 100,

    [switch] $Rebuild
)

$ErrorActionPreference = "Stop"

$Context = "docker-desktop"
$Namespace = "messaging"
$Image = "messaging-backend:local"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Invoke-Kubectl {
    & kubectl --context=$Context @args
}

function Require-File {
    param([string] $Path)

    if (-not (Test-Path $Path)) {
        throw "Missing required file: $Path"
    }
}

function Build-Backend {
    Push-Location $Root
    try {
        docker build -t $Image .\backend
    }
    finally {
        Pop-Location
    }
}

function Apply-Config {
    Require-File (Join-Path $Root "k8s\.env")
    Require-File (Join-Path $Root "k8s\secrets.env")

    & kubectl --context=$Context create namespace $Namespace --dry-run=client -o yaml |
        & kubectl --context=$Context apply -f -

    & kubectl --context=$Context -n $Namespace create configmap messaging-backend-config --from-env-file=k8s\.env --dry-run=client -o yaml |
        & kubectl --context=$Context apply -f -

    & kubectl --context=$Context -n $Namespace create secret generic messaging-backend-secrets --from-env-file=k8s\secrets.env --dry-run=client -o yaml |
        & kubectl --context=$Context apply -f -
}

function Start-App {
    Push-Location $Root
    try {
        Apply-Config

        Invoke-Kubectl -n $Namespace apply -f k8s\postgres.yaml -f k8s\redis.yaml -f k8s\kafka.yaml
        Invoke-Kubectl -n $Namespace rollout status statefulset/postgres --timeout=600s
        Invoke-Kubectl -n $Namespace rollout status statefulset/redis --timeout=600s
        Invoke-Kubectl -n $Namespace rollout status statefulset/kafka --timeout=600s

        Invoke-Kubectl -n $Namespace apply -f k8s\backend.yaml
        Invoke-Kubectl apply -f k8s\local\backend-service.yaml
        Invoke-Kubectl -n $Namespace rollout status deployment/messaging-backend --timeout=300s
    }
    finally {
        Pop-Location
    }
}

function Stop-App {
    Invoke-Kubectl -n $Namespace scale deployment/messaging-backend --replicas=0
    Invoke-Kubectl -n $Namespace scale statefulset/kafka --replicas=0
    Invoke-Kubectl -n $Namespace scale statefulset/redis --replicas=0
    Invoke-Kubectl -n $Namespace scale statefulset/postgres --replicas=0
}

function Restart-Backend {
    if ($Rebuild) {
        Build-Backend
    }

    Invoke-Kubectl -n $Namespace rollout restart deployment/messaging-backend
    Invoke-Kubectl -n $Namespace rollout status deployment/messaging-backend --timeout=300s
}

switch ($Command) {
    "start" {
        if ($Rebuild) {
            Build-Backend
        }
        Start-App
        Invoke-Kubectl -n $Namespace get pods
    }
    "stop" {
        Stop-App
        Invoke-Kubectl -n $Namespace get pods
    }
    "restart" {
        Restart-Backend
        Invoke-Kubectl -n $Namespace get pods -l app=messaging-backend
    }
    "status" {
        Invoke-Kubectl -n $Namespace get pods
        Invoke-Kubectl -n $Namespace get service messaging-backend
    }
    "metrics" {
        Invoke-Kubectl -n $Namespace top pods
    }
    "logs" {
        if ($Pod) {
            Invoke-Kubectl -n $Namespace logs $Pod --tail=$Tail
        }
        else {
            Invoke-Kubectl -n $Namespace logs -l app=messaging-backend --all-containers=true --tail=$Tail --prefix=true
        }
    }
}
