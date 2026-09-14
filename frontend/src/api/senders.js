import client from './client'

export const createSender = (data) => client.post('/senders', data).then((r) => r.data)
export const getSendersByUser = (userId) => client.get(`/senders/user/${userId}`).then((r) => r.data)
export const updateSender = (id, data) => client.put(`/senders/${id}`, data).then((r) => r.data)
export const deleteSender = (id) => client.delete(`/senders/${id}`)
