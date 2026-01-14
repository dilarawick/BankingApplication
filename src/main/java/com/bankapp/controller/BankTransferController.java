package com.bankapp.controller;

import com.bankapp.dto.BankTransferRequest;
import com.bankapp.dto.BankTransferResponse;
import com.bankapp.service.BankTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class BankTransferController {

    @Autowired
    private BankTransferService bankTransferService;

    // Endpoint to initiate a bank transfer
    @PostMapping("/initiate")
    public ResponseEntity<BankTransferResponse> initiateTransfer(
            @Valid @RequestBody BankTransferRequest request,
            Authentication authentication) {

        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            BankTransferResponse response = bankTransferService.initiateTransfer(request, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BankTransferResponse errorResponse = new BankTransferResponse(
                    "Transfer failed: " + e.getMessage(), "FAILED");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint to confirm a bank transfer (after user review)
    @PostMapping("/confirm/{transferId}")
    public ResponseEntity<BankTransferResponse> confirmTransfer(
            @PathVariable Integer transferId,
            Authentication authentication) {

        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            BankTransferResponse response = bankTransferService.confirmTransfer(transferId, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BankTransferResponse errorResponse = new BankTransferResponse(
                    "Transfer confirmation failed: " + e.getMessage(), "FAILED");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint to get transfer history for a customer
    @GetMapping("/history")
    public ResponseEntity<List<BankTransferResponse>> getTransferHistory(Authentication authentication) {
        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            List<BankTransferResponse> transfers = bankTransferService.getTransferHistory(customerId);
            return ResponseEntity.ok(transfers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint to get details of a specific transfer
    @GetMapping("/{transferId}")
    public ResponseEntity<BankTransferResponse> getTransferDetails(
            @PathVariable Integer transferId,
            Authentication authentication) {

        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            BankTransferResponse response = bankTransferService.getTransferDetails(transferId, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BankTransferResponse errorResponse = new BankTransferResponse(
                    "Failed to retrieve transfer details: " + e.getMessage(), "FAILED");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint to validate transfer data before submission
    @PostMapping("/validate")
    public ResponseEntity<BankTransferResponse> validateTransfer(
            @Valid @RequestBody BankTransferRequest request,
            Authentication authentication) {

        try {
            Integer customerId = (Integer) authentication.getPrincipal();
            BankTransferResponse response = bankTransferService.validateTransfer(request, customerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BankTransferResponse errorResponse = new BankTransferResponse(
                    "Transfer validation failed: " + e.getMessage(), "FAILED");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}