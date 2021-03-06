package org.tint.domain;

import android.view.ContextMenu;
import android.webkit.WebView.HitTestResult;

import org.tint.ui.uihelpers.browser.BrowserActivityContextMenuOptions;
import org.tint.ui.uihelpers.visitors.browser.*;
import org.tint.utils.Predicate;

/**
 * User: Abhijit
 * Date: 2016-06-12
 */
public enum HtmlNode {
    ANCHOR(new Predicate<Integer>() {
        @Override
        public boolean isSatisfiedBy(Integer resultType) {
            if ((resultType == HitTestResult.ANCHOR_TYPE) || (resultType == HitTestResult.IMAGE_ANCHOR_TYPE) ||
                    (resultType == HitTestResult.SRC_ANCHOR_TYPE) ||
                    (resultType == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
                return true;
            }
            return false;
        }
    }) {
        @Override
        protected BrowserActivityContextMenuOptions[] getContextMenuOptions() {
            return new BrowserActivityContextMenuOptions[]{BrowserActivityContextMenuOptions.OPEN,
                    BrowserActivityContextMenuOptions.OPEN_IN_NEW_TAB,
                    BrowserActivityContextMenuOptions.OPEN_IN_BACKGROUND,
                    BrowserActivityContextMenuOptions.COPY,
                    BrowserActivityContextMenuOptions.DOWNLOAD,
                    BrowserActivityContextMenuOptions.SHARE
            };
        }

        @Override
        protected NoopCreateHtmlNodeContextMenuVisitor getCreateContextMenuVisitor(ContextMenu contextMenu, HitTestResult result, boolean isPrivateBrowsingEnabled) {
            return new BrowserCreateAnchorContextMenuVisitor(contextMenu, result, isPrivateBrowsingEnabled);
        }
    }, IMAGE(new Predicate<Integer>() {
        @Override
        public boolean isSatisfiedBy(Integer resultType) {
            return resultType == HitTestResult.IMAGE_TYPE;
        }
    }) {
        @Override
        protected BrowserActivityContextMenuOptions[] getContextMenuOptions() {
            return new BrowserActivityContextMenuOptions[]{BrowserActivityContextMenuOptions.OPEN,
                    BrowserActivityContextMenuOptions.OPEN_IN_NEW_TAB,
                    BrowserActivityContextMenuOptions.COPY,
                    BrowserActivityContextMenuOptions.DOWNLOAD,
                    BrowserActivityContextMenuOptions.SHARE
            };
        }

        @Override
        protected NoopCreateHtmlNodeContextMenuVisitor getCreateContextMenuVisitor(ContextMenu contextMenu, HitTestResult result, boolean isPrivateBrowsingEnabled) {
            return new BrowserCreateImageContextMenuVisitor(contextMenu, result, isPrivateBrowsingEnabled);
        }

    }, EMAIL(new Predicate<Integer>() {
        @Override
        public boolean isSatisfiedBy(Integer resultType) {
            return resultType == HitTestResult.EMAIL_TYPE;
        }
    }) {
        @Override
        protected BrowserActivityContextMenuOptions[] getContextMenuOptions() {
            return new BrowserActivityContextMenuOptions[]{BrowserActivityContextMenuOptions.SEND_MAIL,
                    BrowserActivityContextMenuOptions.COPY,
                    BrowserActivityContextMenuOptions.SHARE
            };
        }

        @Override
        protected NoopCreateHtmlNodeContextMenuVisitor getCreateContextMenuVisitor(ContextMenu contextMenu, HitTestResult result, boolean isPrivateBrowsingEnabled) {
            return new BrowserCreateEmailContextMenuVisitor(contextMenu, result, isPrivateBrowsingEnabled);
        }

    }, DEFAULT(new Predicate<Integer>() {
        @Override
        public boolean isSatisfiedBy(Integer integer) {
            return true;
        }
    }) {
        @Override
        protected BrowserActivityContextMenuOptions[] getContextMenuOptions() {
            return new BrowserActivityContextMenuOptions[0];
        }

        @Override
        protected NoopCreateHtmlNodeContextMenuVisitor getCreateContextMenuVisitor(ContextMenu contextMenu, HitTestResult result, boolean isPrivateBrowsingEnabled) {
            return new NoopCreateHtmlNodeContextMenuVisitor();
        }
    };
    private final Predicate<Integer> predicate;

    HtmlNode(Predicate<Integer> predicate) {
        this.predicate = predicate;
    }

    public static HtmlNode getFromResultType(int resultType) {
        for (HtmlNode htmlNode : values()) {
            if (htmlNode.predicate.isSatisfiedBy(resultType)) {
                return htmlNode;
            }
        }
        return DEFAULT;
    }

    protected abstract BrowserActivityContextMenuOptions[] getContextMenuOptions();

    protected abstract NoopCreateHtmlNodeContextMenuVisitor getCreateContextMenuVisitor(ContextMenu contextMenu, HitTestResult result, boolean isPrivateBrowsingEnabled);

    public final void execute(ContextMenu contextMenu, String parentFragmentUUid, HitTestResult result, boolean
            isPrivateBrowsingEnabled) {
        NoopCreateHtmlNodeContextMenuVisitor browserCreateAnchorContextMenuVisitor = getCreateContextMenuVisitor(contextMenu, result, isPrivateBrowsingEnabled);
        for (BrowserActivityContextMenuOptions browserActivityContextMenuOptions : getContextMenuOptions()) {
            browserActivityContextMenuOptions.accept(browserCreateAnchorContextMenuVisitor);
        }
        browserCreateAnchorContextMenuVisitor.createContributedContextMenu(contextMenu, parentFragmentUUid, result.getType(), result.getExtra(),
                isPrivateBrowsingEnabled);
        browserCreateAnchorContextMenuVisitor.setHeaderTitle(contextMenu, result.getExtra());
    }
}
