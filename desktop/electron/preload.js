'use strict';

/**
 * Мост между страницей Wonder и Electron.
 * Ничего лишнего: только управление окном, автозапуск и обновления.
 */

const { contextBridge, ipcRenderer } = require('electron');

function appVersion() {
  try {
    return ipcRenderer.sendSync('wonder:version');
  } catch {
    return '1.0.0';
  }
}

contextBridge.exposeInMainWorld('wonderDesktop', {
  version: appVersion(),
  platform: process.platform,
  isElectron: true,

  minimize: () => ipcRenderer.invoke('wonder:minimize'),
  hide: () => ipcRenderer.invoke('wonder:hide'),
  quit: () => ipcRenderer.invoke('wonder:quit'),

  toggleAutostart: (value) => ipcRenderer.invoke('wonder:toggle-autostart', value),
  getAutostart: () => ipcRenderer.invoke('wonder:get-autostart'),
  checkUpdates: () => ipcRenderer.invoke('wonder:check-updates'),
  getUpdateStatus: () => ipcRenderer.invoke('wonder:get-update-status'),
  downloadUpdate: () => ipcRenderer.invoke('wonder:download-update'),
  installUpdate: () => ipcRenderer.invoke('wonder:install-update'),

  onUpdateStatus: (cb) => {
    if (typeof cb !== 'function') return () => {};
    const handler = (_event, payload) => {
      try {
        cb(payload);
      } catch {
        /* ошибка в колбэке страницы не должна ронять мост */
      }
    };
    ipcRenderer.on('wonder:update-status', handler);
    return () => ipcRenderer.removeListener('wonder:update-status', handler);
  },
});
