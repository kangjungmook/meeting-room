import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage, isSupported } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: "AIzaSyBd6PbHimM0EBkmJD7FLA0IdkgeSWNmXmw",
  authDomain: "meeting-room-403e7.firebaseapp.com",
  projectId: "meeting-room-403e7",
  storageBucket: "meeting-room-403e7.firebasestorage.app",
  messagingSenderId: "434586638221",
  appId: "1:434586638221:web:c4d2086ed726ca733e0fc4"
};

export const VAPID_KEY = 'BADCjobntzfgKPcB7tFfxn4ZtgljdWo3OPmlV2HOk55Ra1VeNEdF42TX4BV7kDQhCkSwRqmqiWDBu2oQFt3EX3g';

const app = initializeApp(firebaseConfig);

// messaging은 지연 초기화 — 브라우저 지원 여부 확인 후 사용
let _messaging = null;

export const getMessagingInstance = async () => {
  if (_messaging) return _messaging;
  const supported = await isSupported();
  if (!supported) return null;
  _messaging = getMessaging(app);
  return _messaging;
};

export { getToken, onMessage };
