package com.rbkmoney.hooker.endpoint;

import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.woody.api.event.CompositeServiceEventListener;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.HttpServiceEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/hook")
@Slf4j
@RequiredArgsConstructor
public class HookerServlet extends GenericServlet {

    private Servlet thriftServlet;

    private final WebhookManagerSrv.Iface requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.info("Hooker servlet init.");
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .withEventListener(
                        new CompositeServiceEventListener<>(
                            new ServiceEventLogListener(),
                            new HttpServiceEventLogListener()
                        )
                )
                .build(WebhookManagerSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        log.info("Start new request to servlet.");
        thriftServlet.service(req, res);
    }
}
