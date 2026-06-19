importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyBd6PbHimM0EBkmJD7FLA0IdkgeSWNmXmw",
  authDomain: "meeting-room-403e7.firebaseapp.com",
  projectId: "meeting-room-403e7",
  storageBucket: "meeting-room-403e7.firebasestorage.app",
  messagingSenderId: "434586638221",
  appId: "1:434586638221:web:c4d2086ed726ca733e0fc4"
});

const messaging = firebase.messaging();

// notification 필드가 있는 메시지는 브라우저가 자동으로 알림을 표시함
// onBackgroundMessage에서 showNotification을 호출하면 중복으로 2개가 뜨므로 제거
messaging.onBackgroundMessage((payload) => {
  // data-only 메시지일 경우에만 직접 표시
  if (!payload.notification && payload.data) {
    const title = payload.data.title ?? '알림';
    const body  = payload.data.body  ?? '';
    self.registration.showNotification(title, {
      body,
      icon: '/favicon.ico',
      badge: '/favicon.ico',
    });
  }
});
