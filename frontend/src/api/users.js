import client from './client'

export const registerUser = (data) => client.post('/users/register', data).then((r) => r.data)
export const getUsers = () => client.get('/users').then((r) => r.data)
export const deactivateUser = (id) => client.patch(`/users/${id}/deactivate`).then((r) => r.data)

// "Who am I" for the HTTP Basic credentials currently attached to `client` — there is no
// dedicated login endpoint, this is how the frontend confirms a login attempt succeeded.
export const getMe = () => client.get('/users/me').then((r) => r.data)
