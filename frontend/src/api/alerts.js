import client from './client'

export const createAlert = (data) => client.post('/alerts', data).then((r) => r.data)
export const getActiveAlertsByUser = (userId) => client.get(`/alerts/user/${userId}`).then((r) => r.data)
export const getAllAlertsByUser = (userId) => client.get(`/alerts/user/${userId}/all`).then((r) => r.data)
export const deactivateAlert = (id) => client.patch(`/alerts/${id}/deactivate`).then((r) => r.data)
export const deleteAlert = (id) => client.delete(`/alerts/${id}`)
