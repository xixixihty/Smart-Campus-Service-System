import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

let stompClient = null
let connected = ref(false)
let connectPromise = null
let subscriptions = []
let subscriberCount = 0

export function useWebSocket() {
  const localConnected = connected

  const connect = () => {
    if (connectPromise) {
      return connectPromise
    }

    connectPromise = new Promise((resolve, reject) => {
      const token = localStorage.getItem('token')
      if (!token) {
        connectPromise = null
        reject(new Error('未登录'))
        return
      }

      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsUrl = `${protocol}//${window.location.host}/api/ws/websocket`

      stompClient = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: () => {
          connected.value = true
          resolve()
        },
        onStompError: (frame) => {
          console.error('STOMP错误:', frame.headers['message'])
          connectPromise = null
          reject(new Error(frame.headers['message']))
        },
        onWebSocketClose: () => {
          connected.value = false
        }
      })

      stompClient.activate()
    })

    return connectPromise
  }

  const subscribe = (destination, callback) => {
    if (!stompClient || !stompClient.connected) {
      console.warn('STOMP未连接，无法订阅')
      return null
    }

    const userDestination = `/user${destination}`

    const subscription = stompClient.subscribe(userDestination, (message) => {
      try {
        const payload = JSON.parse(message.body)
        callback(payload)
      } catch (e) {
        callback(message.body)
      }
    })

    subscriptions.push(subscription)
    return subscription
  }

  const disconnect = () => {
    subscriptions.forEach(sub => {
      try { sub.unsubscribe() } catch (e) { /* ignore */ }
    })
    subscriptions.length = 0

    if (stompClient) {
      try { stompClient.deactivate() } catch (e) { /* ignore */ }
      stompClient = null
      connectPromise = null
    }
    connected.value = false
  }

  subscriberCount++
  onUnmounted(() => {
    subscriberCount--
    if (subscriberCount <= 0) {
      disconnect()
    }
  })

  return {
    connected: localConnected,
    connect,
    subscribe,
    disconnect
  }
}