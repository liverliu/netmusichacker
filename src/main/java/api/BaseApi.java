package api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by liverliu on 1/12/16.
 */
public abstract class BaseApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApi.class);

    @RequestMapping(value = "*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route1(HttpServletRequest request) {
        LOGGER.info(request.getServletPath());
        LOGGER.info(request.getMethod());
        return "fuck";
    }

    @RequestMapping(value = "*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route2(HttpServletRequest request) {
        LOGGER.info(request.getServletPath());
        LOGGER.info(request.getMethod());
        return "fuck";
    }

    @RequestMapping(value = "*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route3(HttpServletRequest request) {
        LOGGER.info(request.getServletPath());
        LOGGER.info(request.getMethod());
        return "fuck";
    }

    @RequestMapping(value = "*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route4(HttpServletRequest request) {
        LOGGER.info(request.getServletPath());
        LOGGER.info(request.getMethod());
        return "fuck";
    }

    @RequestMapping(value = "*/*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route5(HttpServletRequest request) {
        LOGGER.info(request.getServletPath());
        LOGGER.info(request.getMethod());
        return "fuck";
    }
}
