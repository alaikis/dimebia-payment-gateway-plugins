package com.alaikis.paymenthub.repo;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
import be.woutschoovaerts.mollie.data.common.AddressRequest;
import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.payment.PaymentLineRequest;
import be.woutschoovaerts.mollie.data.payment.PaymentMethod;
import be.woutschoovaerts.mollie.data.payment.PaymentRequest;
import com.alaikis.paymenthub.entity.Result;
import com.alaikis.paymenthub.entity.ResultUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Payment {

    private Amount amountGenerator(String currency, BigDecimal value){
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setValue(value);
        return amount;
    }

    /**
     * payment apply
     */
    public Result paymentApply(ObjectNode authParams, ObjectNode transaction, ObjectNode args) throws Exception {
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());  // for Java 8 date/time types
            mapper.registerModule(new Jdk8Module());      // for Optional and other JDK 8 types
            JsonNode authParam = mapper.readTree(authParams.path("property").asText());
            Client client = new ClientBuilder()
                    .withApiKey(authParam.path("apiKey").asText())
                    .build();
            PaymentRequest request = new PaymentRequest();
            request.setAmount(amountGenerator(
                            transaction.path("amount").path("currency").asText(),
                            new BigDecimal(transaction.path("amount").path("value").asText())
                    )
            );
            if (transaction.has("paymentMethod") && !transaction.path("paymentMethod").isNull()) {
                List<PaymentMethod> method = Arrays.stream(transaction.path("paymentMethod").asText().split(",")).map(
                                PaymentMethod::valueOf
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!method.isEmpty()) {
                    request.setMethod(Optional.of(method));
                }
            }
            request.setDescription(transaction.path("description").asText());
            request.setRedirectUrl(transaction.path("redirectUrl").asText());
            if (transaction.has("webhookUrl") && !transaction.path("webhookUrl").isNull()){
                request.setWebhookUrl(Optional.of(transaction.path("webhookUrl").asText()));
            }
            if (!transaction.path("billingAddress").isNull()){
                JsonNode baseAddress = transaction.path("billingAddress");
                AddressRequest billingAddress = new AddressRequest();
                billingAddress.setCountry(baseAddress.path("country").asText().toLowerCase(Locale.ROOT));
                billingAddress.setCity(baseAddress.path("city").asText());
                billingAddress.setGivenName(baseAddress.path("givenName").asText());
                if (baseAddress.has("familyName") && !baseAddress.path("familyName").isNull()){
                    billingAddress.setFamilyName(baseAddress.path("familyName").asText());
                }
                if (baseAddress.has("email") && !baseAddress.path("email").isNull()) {
                    billingAddress.setEmail(baseAddress.path("email").asText());
                }
                if (baseAddress.has("postalCode") && !baseAddress.path("postalCode").isNull()) {
                    billingAddress.setPostalCode(Optional.ofNullable(baseAddress.path("postalCode").asText()));
                }
                if (baseAddress.has("tel") && !baseAddress.path("tel").isNull()) {
                    billingAddress.setPhone(baseAddress.path("tel").asText());
                }
                billingAddress.setStreetAndNumber(baseAddress.path("address1").asText());
                request.setBillingAddress(Optional.of(billingAddress));
            }
            if (!transaction.path("shippingAddress").isNull()){
                JsonNode baseAddress = transaction.path("shippingAddress");
                AddressRequest shippingAddress = new AddressRequest();
                shippingAddress.setCountry(baseAddress.path("country").asText().toLowerCase(Locale.ROOT));
                shippingAddress.setCity(baseAddress.path("city").asText());
                shippingAddress.setGivenName(baseAddress.path("givenName").asText());
                if (baseAddress.has("familyName") && !baseAddress.path("familyName").isNull()){
                    shippingAddress.setFamilyName(baseAddress.path("familyName").asText());
                }
                if (baseAddress.has("email") && !baseAddress.path("email").isNull()) {
                    shippingAddress.setEmail(baseAddress.path("email").asText());
                }
                if (baseAddress.has("postalCode") && !baseAddress.path("postalCode").isNull()) {
                    shippingAddress.setPostalCode(Optional.ofNullable(baseAddress.path("postalCode").asText()));
                }
                if (baseAddress.has("tel") && !baseAddress.path("tel").isNull()) {
                    shippingAddress.setPhone(baseAddress.path("tel").asText());
                }
                shippingAddress.setStreetAndNumber(baseAddress.path("address1").asText());
                request.setShippingAddress(Optional.of(shippingAddress));
            }
            if (transaction.has("locale") && !transaction.path("locale").isNull()) {
                request.setLocale(Optional.of(be.woutschoovaerts.mollie.data.common.Locale.valueOf(transaction.path("locale").asText())));
            }
            if (!transaction.path("lines").isEmpty()) {
                List<PaymentLineRequest> lineRequests = new java.util.ArrayList<>(List.of());
                /**
                 * {
                 *     "sku": "5702016116977",
                 *     "name": "LEGO 42083 Bugatti Chiron",
                 *     "productUrl": "https://shop.lego.com/nl-NL/Bugatti-Chiron-42083",
                 *     "imageUrl": "https://sh-s7-live-s.legocdn.com/is/image//LEGO/42083_alt1?$main$",
                 *     "quantity": 2,
                 *     "vatRate": "21.00",
                 *     "unitPrice": {
                 *         "currency": "EUR",
                 *         "value": "399.00"
                 *     },
                 *     "totalAmount": {
                 *         "currency": "EUR",
                 *         "value": "698.00"
                 *     },
                 *     "discountAmount": {
                 *         "currency": "EUR",
                 *         "value": "100.00"
                 *     },
                 *     "vatAmount": {
                 *         "currency": "EUR",
                 *         "value": "121.14"
                 *     }
                 * }
                 */
                for (JsonNode line : transaction.path("line")) {
                    PaymentLineRequest request1 = new PaymentLineRequest();
                    request1.setDescription(line.path("name").asText());
                    request1.setQuantity(line.path("quantity").asInt());
                    if (line.has("imageUrl") && line.path("imageUrl")!= null) {
                        request1.setImageUrl(Optional.ofNullable(line.path("imageUrl").asText()));
                    }
                    if (line.has("productUrl") && line.path("productUrl")!= null) {
                        request1.setProductUrl(Optional.ofNullable(line.path("productUrl").asText()));
                    }

                    request1.setUnitPrice(amountGenerator(
                            line.path("unitPrice").path("currency").asText(),
                            new BigDecimal(line.path("unitPrice").path("value").asText())
                    ));

                    if (line.has("discountAmount") && !line.path("discountAmount").isNull()) {
                        request1.setDiscountAmount(Optional.of(amountGenerator(
                                line.path("discountAmount").path("currency").asText(),
                                new BigDecimal(line.path("discountAmount").path("value").asText())
                        )));
                    }

                    if (line.has("vatAmount") && !line.path("vatAmount").isNull()) {
                        request1.setVatAmount(Optional.of(amountGenerator(
                                line.path("vatAmount").path("currency").asText(),
                                new BigDecimal(line.path("vatAmount").path("value").asText())
                        )));
                    }


                    request1.setTotalAmount(amountGenerator(
                            line.path("totalAmount").path("currency").asText(),
                            new BigDecimal(line.path("totalAmount").path("value").asText())
                    ));

                    lineRequests.add(request1);
                }
                request.setLines(Optional.of(lineRequests));
            }

            return ResultUtil.success(mapper.valueToTree(client.payments().createPayment(request)));
        }catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage());
        }

    }

    public Result paymentApply(ObjectNode authParams, ObjectNode transaction) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return paymentApply(authParams, transaction, (ObjectNode) mapper.readTree("{}"));
    }

    public Result payments(ObjectNode authParams, ObjectNode filter) throws Exception {return ResultUtil.success("");}

    public Result paymentCancel(ObjectNode authParams, ObjectNode transaction) throws Exception {return ResultUtil.success("");}

}
