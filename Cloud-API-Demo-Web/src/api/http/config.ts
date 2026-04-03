const DEFAULT_BACKEND_PORT = '6789'

function normalizeBaseUrl (): string {
  const fallbackProtocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
  const rawHost = import.meta.env.VITE_APP_APIGATEWAY_BACKEND_HOST || `${fallbackProtocol}//${window.location.hostname}:${DEFAULT_BACKEND_PORT}`

  try {
    const url = new URL(rawHost)
    if (url.hostname === 'localhost' || url.hostname === '127.0.0.1') {
      url.hostname = window.location.hostname
    }
    if (!url.port) {
      url.port = DEFAULT_BACKEND_PORT
    }
    return `${url.protocol}//${url.host}/`
  } catch {
    return `${fallbackProtocol}//${window.location.hostname}:${DEFAULT_BACKEND_PORT}/`
  }
}

const RESOLVED_BASE_URL = normalizeBaseUrl()
const RESOLVED_URL = new URL(RESOLVED_BASE_URL)
const WS_PROTOCOL = RESOLVED_URL.protocol === 'https:' ? 'wss:' : 'ws:'

export const CURRENT_CONFIG = {

  // license
  appId: '180899', // You need to go to the development website to apply.
  appKey: 'dfba9e99a68baaffe46c45542e963bc', // You need to go to the development website to apply.
  appLicense: 'oGCCQBhmxfKZgNnc4tk1blM1N3xHeoaBNyfzPB7EBriJuqSYJjeCMXIS0eojj/gSCFQ2303BpNrY68XD+DqVuTlvmV30to1OkRto1O8cCYzv+uVBtdcyCFbQAkVjeG/RAr0fHAFdRpBI/Po8xnkPz8DTXZX9EcSftU/pY+LR8r8=', // You need to go to the development website to apply.

  // http
  baseURL: RESOLVED_BASE_URL,
  websocketURL: `${WS_PROTOCOL}//${RESOLVED_URL.host}/api/v1/ws`,

  // livestreaming
  // RTMP  Note: This IP is the address of the streaming server. If you want to see livestream on web page, you need to convert the RTMP stream to WebRTC stream.
  rtmpURL: `rtmp://${RESOLVED_URL.hostname}/live/`,
  // GB28181 Note:If you don't know what these parameters mean, you can go to Pilot2 and select the GB28181 page in the cloud platform. Where the parameters same as these parameters.
  gbServerIp: 'Please enter the server ip.',
  gbServerPort: 'Please enter the server port.',
  gbServerId: 'Please enter the server id.',
  gbAgentId: 'Please enter the agent id',
  gbPassword: 'Please enter the agent password',
  gbAgentPort: 'Please enter the local port.',
  gbAgentChannel: 'Please enter the channel.',
  // RTSP
  rtspUserName: 'Please enter the username.',
  rtspPassword: 'Please enter the password.',
  rtspPort: '8554',
  // Agora
  agoraAPPID: 'Please enter the agora app id.',
  agoraToken: 'Please enter the agora temporary token.',
  agoraChannel: 'Please enter the agora channel.',

  // map
  // You can apply on the AMap website.
  amapKey: 'Please enter the amap key.',

}
