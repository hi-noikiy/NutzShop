package cn.wizzer.modules.controllers.platform.shop.goods;

import cn.wizzer.common.annotation.SLog;
import cn.wizzer.common.base.Result;
import cn.wizzer.common.filter.PrivateFilter;
import cn.wizzer.common.page.DataTableColumn;
import cn.wizzer.common.page.DataTableOrder;
import cn.wizzer.common.util.StringUtil;
import cn.wizzer.modules.models.shop.*;
import cn.wizzer.modules.services.shop.goods.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.*;
import org.nutz.dao.Chain;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@IocBean
@At("/platform/shop/goods/goods")
@Filters({@By(type = PrivateFilter.class)})
public class ShopGoodsController {
    private static final Log log = Logs.get();
    @Inject
    private ShopGoodsService shopGoodsService;
    @Inject
    private ShopGoodsTypeService shopGoodsTypeService;
    @Inject
    private ShopGoodsClassService shopGoodsClassService;
    @Inject
    private ShopGoodsSpecService shopGoodsSpecService;
    @Inject
    private ShopGoodsTypeParamgService shopGoodsTypeParamgService;
    @Inject
    private ShopGoodsTypePropsService shopGoodsTypePropsService;
    @Inject
    private ShopGoodsTypeBrandService shopGoodsTypeBrandService;
    @Inject
    private ShopGoodsTypeSpecService shopGoodsTypeSpecService;
    @Inject
    private ShopGoodsProductsService shopGoodsProductsService;
    @Inject
    private ShopMemberLvService shopMemberLvService;
    @Inject
    private ShopGoodsLvPriceService shopGoodsLvPriceService;
    @Inject
    private ShopGoodsImagesService shopGoodsImagesService;

    @At("")
    @Ok("beetl:/platform/shop/goods/goods/index.html")
    @RequiresAuthentication
    public void index(HttpServletRequest req) {

    }

    /**
     * 获取商品分类及分类的商品类型
     *
     * @param id
     * @return
     */
    @At("/getClass/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getClass(String id) {
        return Result.success("", shopGoodsClassService.fetchLinks(shopGoodsClassService.fetch(id), "goodsType"));
    }

    /**
     * 获取商品类型信息
     *
     * @param id
     * @return
     */
    @At("/getType/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getType(String id) {
        return Result.success("", shopGoodsTypeService.fetch(id));
    }

    /**
     * 获取商品类型的扩展属性
     *
     * @param id
     * @return
     */
    @At("/getProps/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getProps(String id) {
        List<Shop_goods_type_props> list = shopGoodsTypePropsService.query(Cnd.where("typeId", "=", id).asc("location"));
        for (Shop_goods_type_props props : list) {
            shopGoodsTypePropsService.fetchLinks(props, "propsValues", Cnd.orderBy().asc("location"));
        }
        return Result.success("", list);
    }

    /**
     * 获取商品类型的详细参数
     *
     * @param id
     * @return
     */
    @At("/getParam/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getParam(String id) {
        List<Shop_goods_type_paramg> list = shopGoodsTypeParamgService.query(Cnd.where("typeId", "=", id).asc("location"));
        for (Shop_goods_type_paramg paramg : list) {
            shopGoodsTypeParamgService.fetchLinks(paramg, "params", Cnd.orderBy().asc("location"));
        }
        return Result.success("", list);
    }

    /**
     * 通过商品类型获取品牌
     *
     * @param id
     * @return
     */
    @At("/getBrand/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getBrand(String id) {
        List<Shop_goods_type_brand> list = shopGoodsTypeBrandService.query(Cnd.where("typeId", "=", id).asc("location"));
        for (Shop_goods_type_brand brand : list) {
            shopGoodsTypeBrandService.fetchLinks(brand, "brand", Cnd.orderBy().asc("location"));
        }
        return Result.success("", list);
    }

    /**
     * 商品添加页面
     *
     * @param req
     */
    @At
    @Ok("beetl:/platform/shop/goods/goods/add.html")
    @RequiresAuthentication
    public void add(HttpServletRequest req) {
        req.setAttribute("typeList", shopGoodsTypeService.query());
        req.setAttribute("lvList", shopMemberLvService.query());
    }

    /**
     * 开启规格页面
     *
     * @param id
     * @param sku
     * @param req
     */
    @At({"/spec/?/?", "/spec/?/"})
    @Ok("beetl:/platform/shop/goods/goods/spec.html")
    @RequiresAuthentication
    public void spec(String id, String sku, HttpServletRequest req) {
        List<String> ids = new ArrayList<>();
        List<Shop_goods_type_spec> typeSpecList = shopGoodsTypeSpecService.query(Cnd.where("typeId", "=", id).asc("location"));
        for (Shop_goods_type_spec spec : typeSpecList) {
            ids.add(spec.getSpecId());
        }
        List<Shop_goods_spec> list = shopGoodsSpecService.query(Cnd.where("id", "in", ids));
        for (Shop_goods_spec spec : list) {
            shopGoodsSpecService.fetchLinks(spec, "specValues", Cnd.orderBy().asc("location"));
        }
        if (Strings.isEmpty(Strings.sNull(sku).trim())) {
            sku = shopGoodsProductsService.getSkuPrefix();
        }
        req.setAttribute("sku", sku);
        req.setAttribute("specList", list);
        req.setAttribute("lvList", shopMemberLvService.query());
    }


    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.add")
    @SLog(tag = "新建商品", msg = "商品名称:${args[0].name}")
    @AdaptBy(type = WhaleAdaptor.class)
    //uploadifive上传文件后contentTypy改变,需要用WhaleAdaptor接收参数
    public Object addDo(@Param("..") Shop_goods shopGoods, @Param("products") String products, @Param("spec_values") String spec_values, @Param("prop_values") String prop_values, @Param("param_values") String param_values,
                        @Param("images") String images,
                        HttpServletRequest req) {
        try {
            return Result.success("system.success", shopGoodsService.add(shopGoods, products, spec_values, prop_values, param_values, images));
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/shop/goods/goods/edit.html")
    @RequiresAuthentication
    public Object edit(String id) {
        return shopGoodsService.fetch(id);
    }

    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.edit")
    @SLog(tag = "修改商品", msg = "商品名称:${args[0].name}")
    public Object editDo(@Param("..") Shop_goods shopGoods, HttpServletRequest req) {
        try {

            shopGoods.setOpAt((int) (System.currentTimeMillis() / 1000));
            shopGoodsService.updateIgnoreNull(shopGoods);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }

    /**
     * 商品列表页
     *
     * @param length
     * @param start
     * @param draw
     * @param order
     * @param columns
     * @return
     */
    @At
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        return shopGoodsService.data(length, start, draw, order, columns, cnd, null);
    }


    @At({"/delete", "/delete/?"})
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.delete")
    @SLog(tag = "删除商品", msg = "ID:${args[2].getAttribute('id')}")
    public Object delete(String id, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            if (ids != null && ids.length > 0) {
                shopGoodsService.delete(ids);
                req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
            } else {
                shopGoodsService.delete(id);
                req.setAttribute("id", id);
            }
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }


}