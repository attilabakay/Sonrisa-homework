import client from './client'

export const getNotificationAttempts = () => client.get('/notification-attempts').then((r) => r.data)
export const getNotificationAttemptsByAlert = (alertId) =>
  client.get(`/notification-attempts/alert/${alertId}`).then((r) => r.data)
