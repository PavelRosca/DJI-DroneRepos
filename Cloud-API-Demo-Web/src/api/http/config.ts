export const CURRENT_CONFIG = {

  // license
  appId: '180899', // You need to go to the development website to apply.
  appKey: 'dfba9e99a68baaffe46c45542e963bc', // You need to go to the development website to apply.
  appLicense: 'oGCCQBhmxfKZgNnc4tk1blM1N3xHeoaBNyfzPB7EBriJuqSYJjeCMXIS0eojj/gSCFQ2303BpNrY68XD+DqVuTlvmV30to1OkRto1O8cCYzv+uVBtdcyCFbQAkVjeG/RAr0fHAFdRpBI/Po8xnkPz8DTXZX9EcSftU/pY+LR8r8=', // You need to go to the development website to apply.

  // http
  baseURL: `http://${window.location.hostname}:6789/`, // Dynamic IP detection
  websocketURL: `ws://${window.location.hostname}:6789/api/v1/ws`, // Dynamic IP detection

  // livestreaming
  // RTMP  Note: This IP is the address of the streaming server. If you want to see livestream on web page, you need to convert the RTMP stream to WebRTC stream.
  rtmpURL: `rtmp://${window.location.hostname}/live/`, // Dynamic IP detection
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
