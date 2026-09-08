# Single-instance backend on Kubernetes

This deploys the existing backend Docker image with `replicas: 1`, a ClusterIP
Service on port 8080, startup/liveness probes, and readiness using the existing
public `/actuator/health` endpoint. No autoscaler is configured. `Recreate` stops
the old pod before starting the new one during deployments, so updates have
downtime. This is not a distributed singleton lock in failure scenarios.

The manifests also deploy one PostgreSQL, one Redis, and one Kafka instance with
persistent volumes. SMTP must be provided separately. The backend keeps its
1 GiB memory and 2 CPU limits; dependencies have their own resource limits.
The cluster needs a default StorageClass with dynamic volume provisioning and
capacity for 22 GiB of requested storage. These are initial single-node services,
with no high availability. Redis and Kafka are internal, unauthenticated services;
use them in a trusted cluster network. Redis uses AOF and a 256 MB no-eviction
memory budget. Kafka retention is 24 hours / 128 MiB per partition (whichever
limit is reached first); monitor disk usage as topics and partitions grow.

## Resource limits

| Workload | CPU limit | Memory limit |
| --- | --- | --- |
| Backend | 2 cores | 1 GiB |
| PostgreSQL | 1 core | 500 MB (`500M`, decimal) |
| Redis | 0.5 core | 512 MiB |
| Kafka | 1 core | 1 GiB |

These are starting budgets for light traffic, not measured workload requirements.
Redis keeps its data budget at 256 MiB to leave room for persistence buffers and
other allocations ([Redis memory guidance](https://redis.io/docs/latest/develop/reference/eviction/)).
Kafka's JVM heap is capped at 512 MiB within its 1 GiB container limit. Monitor
usage and OOM restarts before increasing traffic. Requests reserve scheduling
capacity; the limits above are per-container ceilings, not reserved resources.

## Configure

### Direct local access on port 8001

After deploying the base manifests to Docker Desktop, run:

```powershell
kubectl --context=docker-desktop apply -f k8s/local/backend-service.yaml
```

This local Service override exposes `http://localhost:8001` through Docker Desktop
without a running `kubectl port-forward`. The backend still listens on 8080 inside
its pod. Compose variables such as `BACKEND_HOST_PORT` do not configure Kubernetes
Service ports. Apply this override again after applying `k8s/backend.yaml`, which
defines the base internal Service. Apply the local override only to Docker Desktop;
a LoadBalancer Service on a cloud cluster may provision a billed load balancer.


Run commands from the repository root. Enable Kubernetes in your local cluster
or select an existing cluster, then check the target with:

```powershell
kubectl config current-context
kubectl get nodes
Copy-Item k8s/.env.example k8s/.env
Copy-Item k8s/secrets.env.example k8s/secrets.env
```

Edit both copied files with real connection settings and credentials. They are
git-ignored. The read/write database pools inherit the `SPRING_DATASOURCE_*`
settings; optionally add `APP_DATASOURCE_READ_*` / `APP_DATASOURCE_WRITE_*`
overrides if using separate databases. Use existing JWT keys to preserve tokens.
Values in these files must be literal: kubectl does not expand `${VARIABLE}` and
does not strip surrounding quotes. Do not copy the Compose `.env` unchanged.

The example already points at the included dependency Services. PostgreSQL creates
the `messaging` database and reads the same username/password Secret keys as the
backend, so credentials stay aligned. Changing these keys after database
initialization does not change the password stored in PostgreSQL; rotate it in the
database too. Existing Compose data is not automatically migrated to these volumes.
Set SMTP to a reachable server. Set `JWT_COOKIE_SECURE=true` when serving over
HTTPS, and set the allowed CORS origin to your frontend URL.

## Build and deploy

For local Docker Desktop usage, the helper script is the simplest way to run the
stack:

```powershell
.\k8s\local\messaging.ps1 start -Rebuild
.\k8s\local\messaging.ps1 status
.\k8s\local\messaging.ps1 metrics
.\k8s\local\messaging.ps1 logs
.\k8s\local\messaging.ps1 restart -Rebuild
.\k8s\local\messaging.ps1 stop
```

`start -Rebuild` builds the backend image, creates/updates the ConfigMap and
Secret from `k8s/.env` and `k8s/secrets.env`, starts PostgreSQL, Redis, Kafka,
and the backend, then exposes the backend on `http://localhost:8001`.
`restart -Rebuild` is for backend code changes. It rebuilds only the backend
image and restarts the backend pods; PostgreSQL, Redis, and Kafka keep running.
`stop` scales all project workloads to zero but keeps the namespace, Services,
and persistent volumes.

```powershell
docker build -t messaging-backend:local ./backend
kubectl create namespace messaging --dry-run=client -o yaml | kubectl apply -f -
kubectl -n messaging create configmap messaging-backend-config --from-env-file=k8s/.env --dry-run=client -o yaml | kubectl apply -f -
kubectl -n messaging create secret generic messaging-backend-secrets --from-env-file=k8s/secrets.env --dry-run=client -o yaml | kubectl apply -f -
kubectl -n messaging apply -f k8s/postgres.yaml -f k8s/redis.yaml -f k8s/kafka.yaml
kubectl -n messaging rollout status statefulset/postgres --timeout=600s
kubectl -n messaging rollout status statefulset/redis --timeout=600s
kubectl -n messaging rollout status statefulset/kafka --timeout=600s
kubectl -n messaging apply -f k8s/backend.yaml
kubectl -n messaging rollout status deployment/messaging-backend --timeout=300s
kubectl -n messaging port-forward service/messaging-backend 8001:8080
```

Stop and resolve any failed command before continuing. In particular, wait for
all dependencies to be ready before applying the backend.

The image must be available to cluster nodes before applying. For kind, use
`kind load docker-image messaging-backend:local`; for minikube use
`minikube image load messaging-backend:local`. For a remote cluster, tag and push
the image to your registry and change `image` in `backend.yaml` to that tag
(configure `imagePullSecrets` if the registry is private).

Your previously selected context was `do-nyc1-dev`, a remote cluster. A local
Docker build alone does not make the image available there. For example:

```powershell
docker tag messaging-backend:local YOUR_REGISTRY/messaging-backend:v1
docker push YOUR_REGISTRY/messaging-backend:v1
```

Replace `YOUR_REGISTRY` with your registry path and set the same full image tag in
`backend.yaml` before applying it. Once configuration, secrets, and the image are
ready, `kubectl -n messaging apply -f k8s/` applies all four workloads together.
The backend may restart while its dependencies initialize on a first deployment;
the ordered commands above make startup easier to troubleshoot.

With port-forward running, the backend is available at `http://localhost:8001`,
matching the frontend's example API URL. Stop any Compose backend using port
8001 first. Port-forward is for local access; use your cluster's ingress or load
balancer for shared access.

## Check and update

```powershell
kubectl -n messaging get pods -l app=messaging-backend
kubectl -n messaging get statefulsets,pvc
kubectl -n messaging logs deployment/messaging-backend --tail=100
```

After changing configuration or secrets, rerun the corresponding creation command
above and run `kubectl -n messaging rollout restart deployment/messaging-backend`.
For code updates, build and load/push a new image tag, update the manifest, and
apply again. Keep `replicas: 1` for now. Health readiness reflects the application's
registered health indicators, not a complete end-to-end check of Kafka or email;
TCP liveness only verifies the server port is accepting connections.

## Move PostgreSQL to RDS later

Keep the backend, Redis, and Kafka manifests. Create an RDS PostgreSQL database,
configure network access from your Kubernetes nodes, and migrate the existing
database before switching. Update `SPRING_DATASOURCE_URL` in `k8s/.env` to your
RDS JDBC URL and update the database credentials in `k8s/secrets.env`. Configure
TLS and the RDS CA certificate for your connection. Update any explicit
`APP_DATASOURCE_READ_*` / `APP_DATASOURCE_WRITE_*` overrides as well.

During cutover, stop backend writes (scale the backend to zero), finish the data
migration, recreate the ConfigMap and Secret using the commands above, and apply
`backend.yaml` to restore one replica. Verify the app against RDS before scaling
the PostgreSQL StatefulSet to zero. Stop applying `postgres.yaml` after cutover;
apply the three remaining workload files explicitly. Keep the PostgreSQL volume
until migration and backup verification are complete. StatefulSet volumes are
retained by default when the workload is removed; deleting a PVC can delete its
underlying disk. Switching the URL alone does not migrate existing data.

Image configuration references: [PostgreSQL image](https://hub.docker.com/_/postgres)
and [Apache Kafka Docker examples](https://github.com/apache/kafka/blob/trunk/docker/examples/README.md).

Deployment strategy reference: [Kubernetes Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#recreate-deployment).
