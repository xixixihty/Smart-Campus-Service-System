export const chatStream = (message, context, onMessage, onDone, onError, signal) => {
  const token = localStorage.getItem('token')
  return fetch('/api/ai/user/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ message, context }),
    signal
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    const read = () => {
      reader.read().then(({ done, value }) => {
        if (done) {
          onDone && onDone()
          return
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data) {
              onMessage && onMessage(data)
            }
          }
        }
        read()
      }).catch(err => {
        onError && onError(err)
      })
    }
    read()
  }).catch(err => {
    onError && onError(err)
  })
}
