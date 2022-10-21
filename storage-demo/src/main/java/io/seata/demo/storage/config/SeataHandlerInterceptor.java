package io.seata.demo.storage.config;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SeataHandlerInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeataHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response,
                             @Nullable Object handler) {
        String rootXid = RootContext.getXID();
        String rpcXid = request.getHeader(RootContext.KEY_XID);
        LOGGER.warn("xid in root context: {}, xid in rpc context: {}", rootXid, rpcXid);
        if (rootXid == null && rpcXid != null) {
            RootContext.bind(rpcXid);
            LOGGER.warn("bind rpc xid({}) to root context", rpcXid);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @Nullable HttpServletResponse response,
                                @Nullable Object handler, @Nullable Exception ex) {
        String rpcXid = request.getHeader(RootContext.KEY_XID);
        if (!StringUtils.hasLength(rpcXid)) {
            LOGGER.error("Empty xid in rpc context!");
            return;
        }
        String unbindXid = RootContext.unbind();
        if (!rpcXid.equalsIgnoreCase(unbindXid)) {
            LOGGER.warn("xid was changed in RPC from {} to {}", rpcXid, unbindXid);
            if (unbindXid != null) {
                RootContext.bind(unbindXid);
                LOGGER.warn("bind {} to root context", unbindXid);
            }
        }
    }

}
