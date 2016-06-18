package org.tint.domain.web;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

import org.tint.ui.managers.UIManager;

/**
 * User: Abhijit
 * Date: 2016-06-13
 */
public interface IJsPromptsManager {
    boolean onJsConfirm(WebView view, String url, String message, JsResult result);

    boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);

    boolean onJsAlert(WebView view, String url, String message, JsResult result);

    public static class Factory {
        public static IJsPromptsManager create(UIManager uiManager) {
            return new JsPromptsManager(uiManager);
        }
    }
}
