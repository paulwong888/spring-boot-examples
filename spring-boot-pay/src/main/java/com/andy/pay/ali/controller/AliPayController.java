package com.andy.pay.ali.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.andy.pay.ali.service.AliPayService;
import com.other.common.constants.Constants;
import com.other.common.model.Product;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Mr.lyon
 * @createBy: 2018-06-16 21:44
 **/
@Slf4j
@Controller
@Api(tags ="支付宝支付")
@RequestMapping(value = "/alipay")
public class AliPayController {
	
	@Autowired
	private AliPayService aliPayService;
	
	@ApiOperation(value="支付主页")
	@RequestMapping(value="index",method=RequestMethod.GET)
    public String index() {
        return "alipay/index";
    }


	@ApiOperation(value="电脑支付")
	@RequestMapping(value="pcPay",method=RequestMethod.POST)
    public String  pcPay(Product product, ModelMap map) {
		log.info("电脑支付");
		String form  =  aliPayService.aliPayPc(product);
		map.addAttribute("form", form);
		return "alipay/pay";
    }


	@ApiOperation(value="手机H5支付")
	@RequestMapping(value="mobilePay",method=RequestMethod.POST)
    public String  mobilePay(Product product, ModelMap map) {
		log.info("手机H5支付");
		String form  =  aliPayService.aliPayMobile(product);
		map.addAttribute("form", form);
		return "alipay/pay";
    }


	@ApiOperation(value="二维码支付")
	@RequestMapping(value="qrpay",method=RequestMethod.POST)
    public String  qcPay(Product product, ModelMap map) {
		log.info("二维码支付");
		String message  =  aliPayService.aliPay(product);
		if(Constants.SUCCESS.equals(message)){
			String img= "../qrcode/"+product.getOutTradeNo()+".png";
			map.addAttribute("img", img);
		}else{
			//失败
		}
		return "alipay/qcpay";
    }


	@ApiOperation(value="app支付服务端")
	@RequestMapping(value="appPay",method=RequestMethod.POST)
    public String  appPay(Product product, ModelMap map) {
		log.info("app支付服务端");
		String orderString  =  aliPayService.appPay(product);
		map.addAttribute("orderString", orderString);
		return "alipay/pay";
    }

    /**
     * 支付宝支付回调(二维码、H5、网站)
     */
	@ApiOperation(value="支付宝支付回调(二维码、H5、网站)")
	@RequestMapping(value="pay",method=RequestMethod.POST)
	public void alipay_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String  message = "success";
		Map<String, String> params = new HashMap<String, String>();
		// 取出所有参数是为了验证签名
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			params.put(parameterName, request.getParameter(parameterName));
		}
		//验证签名 校验签名
		boolean signVerified = false;
		try {
			signVerified = AlipaySignature.rsaCheckV1(params, Configs.getAlipayPublicKey(), "UTF-8");
		} catch (AlipayApiException e) {
			e.printStackTrace();
			message =  "failed";
		}
		if (signVerified) {
			log.info("支付宝验证签名成功！");
			// 若参数中的appid和填入的appid不相同，则为异常通知
			if (!Configs.getAppid().equals(params.get("app_id"))) {
				log.info("与付款时的appid不同，此为异常通知，应忽略！");
				message =  "failed";
			}else{
				String outtradeno = params.get("out_trade_no");
				//在数据库中查找订单号对应的订单，并将其金额与数据库中的金额对比，若对不上，也为异常通知
				String status = params.get("trade_status");
				if (status.equals("WAIT_BUYER_PAY")) { // 如果状态是正在等待用户付款
					log.info(outtradeno + "订单的状态正在等待用户付款");
				} else if (status.equals("TRADE_CLOSED")) { // 如果状态是未付款交易超时关闭，或支付完成后全额退款
					log.info(outtradeno + "订单的状态已经关闭");
				} else if (status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")) { // 如果状态是已经支付成功
					log.info("(支付宝订单号:"+outtradeno+"付款成功)");
					//这里 根据实际业务场景 做相应的操作
				} else {

				}
			}
		} else { // 如果验证签名没有通过
			message =  "failed";
			log.info("验证签名失败！");
		}
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(message.getBytes());
		out.flush();
		out.close();
	}
}
