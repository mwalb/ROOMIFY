package org.com

import kotlin.js.ExperimentalWasmJsInterop

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun dateNow(): Double

actual fun currentTimeMillis(): Long = dateNow().toLong()

actual fun getPlatformBaseUrl(): String {
    val hostname = kotlinx.browser.window.location.hostname
    val isLocal = hostname == "localhost" ||
                  hostname == "127.0.0.1" ||
                  hostname.startsWith("192.168.") ||
                  hostname.startsWith("10.") ||
                  hostname.startsWith("172.")
    return if (isLocal) {
        "http://$hostname:8080"
    } else {
        "https://roomifytz-backend.fly.dev"
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
(url) => {
    try {
        let existing = document.getElementById('roomify-video-modal-overlay');
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = 'roomify-video-modal-overlay';
        overlay.style.cssText = 'position:fixed;inset:0;background:rgba(15,23,42,0.88);backdrop-filter:blur(6px);z-index:999999;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:24px;';

        const container = document.createElement('div');
        container.style.cssText = 'position:relative;width:100%;max-width:860px;background:#0F172A;border-radius:20px;overflow:hidden;box-shadow:0 25px 50px -12px rgba(0,0,0,0.65);border:1px solid rgba(255,255,255,0.15);';

        const header = document.createElement('div');
        header.style.cssText = 'display:flex;align-items:center;justify-content:space-between;padding:14px 20px;background:#1E293B;color:#FFFFFF;font-family:Inter,sans-serif;font-weight:700;font-size:15px;';
        header.innerHTML = '<span>Virtual Tour Video</span>';

        const closeBtn = document.createElement('button');
        closeBtn.innerHTML = '&#10005;';
        closeBtn.style.cssText = 'background:rgba(255,255,255,0.12);border:none;color:#FFFFFF;width:32px;height:32px;border-radius:50%;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center;';
        closeBtn.onclick = () => overlay.remove();
        header.appendChild(closeBtn);

        const videoWrapper = document.createElement('div');
        videoWrapper.style.cssText = 'position:relative;width:100%;background:#000000;min-height:320px;max-height:75vh;display:flex;align-items:center;justify-content:center;';

        const video = document.createElement('video');
        video.src = url;
        video.controls = true;
        video.autoplay = true;
        video.playsInline = true;
        video.style.cssText = 'width:100%;max-height:75vh;outline:none;display:block;';

        video.onerror = () => {
            videoWrapper.innerHTML = '<div style="padding:48px 24px;text-align:center;color:#E2E8F0;font-family:Inter,sans-serif;"><div style="font-size:16px;font-weight:700;margin-bottom:8px;">Video Unavailable</div><div style="font-size:13px;color:#94A3B8;">This video file is no longer available on the server or needs to be re-uploaded by the property owner.</div></div>';
        };

        videoWrapper.appendChild(video);
        container.appendChild(header);
        container.appendChild(videoWrapper);
        overlay.appendChild(container);
        overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
        document.body.appendChild(overlay);
        return true;
    } catch (e) {
        console.error('Failed to open video player modal:', e);
        return false;
    }
}
""")
private external fun jsOpenVideoModal(url: String): Boolean

actual fun openVideoPlayerModal(videoUrl: String): Boolean = jsOpenVideoModal(videoUrl)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
(isMapRoute) => {
    console.log('[ROOMIFY ROUTE SYNC] isMapRoute=' + isMapRoute);
    if (isMapRoute) {
        if (typeof window.roomifyShowMap === 'function') {
            window.roomifyShowMap();
        }
    } else {
        if (typeof window.roomifyHideMap === 'function') {
            window.roomifyHideMap();
        }
    }
}
""")
private external fun jsSyncWebMapLayer(isMapRoute: Boolean)

actual fun syncWebMapLayer(isMapRoute: Boolean) = jsSyncWebMapLayer(isMapRoute)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
(message) => {
    if (typeof window.roomifyShowToast === 'function') {
        window.roomifyShowToast(message);
        return true;
    }
    return false;
}
""")
private external fun jsShowNativeWebMapToast(message: String): Boolean

actual fun showNativeWebMapToast(message: String): Boolean = jsShowNativeWebMapToast(message)


