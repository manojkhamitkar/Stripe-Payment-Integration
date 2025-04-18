package com.hulkhiretech.payments.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.hulkhiretech.payments.dto.stripe.StripeEventDTO;
import com.hulkhiretech.payments.pojo.stripe.StripeEvent;
import com.hulkhiretech.payments.service.interfaces.StripeWebhookService;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stripe/webhook")
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookController {
	
	@Value("${stripe.endpointSecret}")
	private String endpointSecret;
	
	private final Gson gson;
	private final ModelMapper modelMapper;
	private final StripeWebhookService stripeWebhookService;
	
	@PostMapping
	public ResponseEntity<String>  processStripeEvent(@RequestBody String eventBody,
			@RequestHeader("Stripe-Signature") String signatureHeader) {
		log.info("Received signature: " + "signatureHeader:" + signatureHeader);

		// identify the checkout session completed event & process that.
		try {
			Webhook.constructEvent(
					eventBody, signatureHeader, endpointSecret);
			log.info("Successfully verified webhook signature");
		} catch (Exception e) {
			log.error("Invalid signature");
			return ResponseEntity.badRequest().build();
		}
		
		// process the event
		// Check evenType
		StripeEvent event = gson.fromJson(eventBody, StripeEvent.class);
		log.info("Received Event type: " + event.getType());
		
		StripeEventDTO eventDto = modelMapper.map(event, StripeEventDTO.class);
		stripeWebhookService.processEvent(eventDto);

		log.info("Event processed successfully");
		return ResponseEntity.ok().build();
	}

}
