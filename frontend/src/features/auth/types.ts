export type AuthUser = {
  userId: string
  username: string
  email: string
  verified: boolean
}

export type RegisterPayload = {
  email: string
  name: string
  username: string
  password: string
  profilePicture?: string | null
}

export type LoginPayload = {
  email: string
  password: string
}
