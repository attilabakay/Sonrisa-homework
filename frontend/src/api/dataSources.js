import client from './client'

export const createDataSource = (data) => client.post('/data-sources', data).then((r) => r.data)
export const getDataSources = () => client.get('/data-sources').then((r) => r.data)
export const updateDataSource = (id, data) => client.put(`/data-sources/${id}`, data).then((r) => r.data)
export const deactivateDataSource = (id) => client.patch(`/data-sources/${id}/deactivate`).then((r) => r.data)
export const deleteDataSource = (id) => client.delete(`/data-sources/${id}`)
export const ingestDataSource = (id) => client.post(`/data-sources/${id}/ingest`).then((r) => r.data)
