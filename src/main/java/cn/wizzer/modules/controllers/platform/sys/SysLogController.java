package cn.wizzer.modules.controllers.platform.sys;

import cn.wizzer.common.annotation.SLog;
import cn.wizzer.common.base.Result;
import cn.wizzer.common.filter.PrivateFilter;
import cn.wizzer.common.page.DataTableColumn;
import cn.wizzer.common.page.DataTableOrder;
import cn.wizzer.modules.services.sys.SysLogService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by wizzer on 2016/6/29.
 */
@IocBean
@At("/platform/sys/log")
@Filters({@By(type = PrivateFilter.class)})
public class SysLogController {
    private static final Log log = Logs.get();
    @Inject
    SysLogService sysLogService;

    @At("")
    @Ok("beetl:/platform/sys/log/index.html")
    @RequiresAuthentication
    public void index() {

    }

    @At
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        return sysLogService.data(length, start, draw, order, columns, cnd, null);
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.log.delete")
    @SLog(tag = "清除日志", msg = "清除日志")
    public Object delete(HttpServletRequest req) {
        try {
            sysLogService.clear();
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }
}
