package io.seata.demo.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class XidRequestInterceptor implements RequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(XidRequestInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        String xid = RootContext.getXID();
        if (!StringUtils.hasLength(xid)) {
            LOGGER.error("Empty xid in root context!");
            return;
        }
        LOGGER.warn("Got xid in root context, xid = " + xid);
        template.header(RootContext.KEY_XID, xid);
    }

}
