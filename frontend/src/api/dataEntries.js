import client from './client'

export const getDataEntries = () => client.get('/data-entries').then((r) => r.data)
export const getDataEntriesBySource = (sourceId) =>
  client.get(`/data-entries/source/${sourceId}`).then((r) => r.data)
