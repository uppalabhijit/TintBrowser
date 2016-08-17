package org.tint.domain.ssl;

/**
 * Created by abhijituppal on 2016-08-16.
 */
public enum SslAuthorityStatus {
    AUTHORITY_UNKNOWN {
        @Override
        public void handleSslError(SslExceptionVisitor sslExceptionVisitor) {
            sslExceptionVisitor.visitAuthorityUnknown();
        }
    }, AUTHORITY_ALLOWED {
        @Override
        public void handleSslError(SslExceptionVisitor sslExceptionVisitor) {
            sslExceptionVisitor.visitAuthorityAllowed();
        }
    }, AUTHORITY_DISALLOWED {
        @Override
        public void handleSslError(SslExceptionVisitor sslExceptionVisitor) {
            sslExceptionVisitor.visitAuthorityDisallowed();
        }
    };

    public abstract void handleSslError(SslExceptionVisitor sslExceptionVisitor);

    public interface SslExceptionVisitor {
        void visitAuthorityUnknown();

        void visitAuthorityAllowed();

        void visitAuthorityDisallowed();
    }
}
