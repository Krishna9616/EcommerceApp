package com.EcommerceApp.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import com.EcommerceApp.model.ProductOrder;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    // ✅ use from properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public Boolean sendMail(String url, String recipientEmail)
            throws UnsupportedEncodingException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setFrom(fromEmail, "Shopping Cart");
        helper.setTo(recipientEmail);

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + url + "\">Change my password</a></p>";

        helper.setSubject("Password Reset");
        helper.setText(content, true);

        mailSender.send(message);
        return true;
    }

    public static String generateUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    public Boolean sendMailForProductOrder(ProductOrder order, String status) throws Exception {

        String msg = "<p>Hello [[name]],</p>"
                + "<p>Your order is <b>[[orderStatus]]</b>.</p>"
                + "<p><b>Product Details:</b></p>"
                + "<p>Name : [[productName]]</p>"
                + "<p>Category : [[category]]</p>"
                + "<p>Quantity : [[quantity]]</p>"
                + "<p>Price : [[price]]</p>"
                + "<p>Payment Type : [[paymentType]]</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setFrom(fromEmail, "Shopping Cart");
        helper.setTo(order.getOrderAddress().getEmail());

        msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
        msg = msg.replace("[[orderStatus]]", status);
        msg = msg.replace("[[productName]]", order.getProduct().getTitle());
        msg = msg.replace("[[category]]", order.getProduct().getCategory());
        msg = msg.replace("[[quantity]]", order.getQuantity().toString());
        msg = msg.replace("[[price]]", order.getPrice().toString());
        msg = msg.replace("[[paymentType]]", order.getPaymentType());

        helper.setSubject("Product Order Status");
        helper.setText(msg, true);

        mailSender.send(message);
        return true;
    }

    public UserDtls getLoggedInUserDetails(Principal p) {
        if (p == null) return null;
        return userService.getUserByEmail(p.getName());
    }
}
