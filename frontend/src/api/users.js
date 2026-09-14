import client from './client'

export const registerUser = (data) => client.post('/users/register', data).then((r) => r.data)
export const getUsers = () => client.get('/users').then((r) => r.data)
export const deactivateUser = (id) => client.patch(`/users/${id}/deactivate`).then((r) => r.data)
