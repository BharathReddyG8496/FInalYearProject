package com.example.finalyearproject.Services;

import com.example.finalyearproject.Abstraction.ConsumerRepo;
import com.example.finalyearproject.Abstraction.DonationRepo;
import com.example.finalyearproject.Abstraction.FarmerRepo;
import com.example.finalyearproject.Abstraction.OrderRepo;
import com.example.finalyearproject.Abstraction.OrderItemRepo;
import com.example.finalyearproject.DataStore.*;
import com.example.finalyearproject.Utility.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationService {

    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

    @Autowired
    private DonationRepo donationRepo;

    @Autowired
    private ConsumerRepo consumerRepo;

    @Autowired
    private FarmerRepo farmerRepo;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderItemRepo orderItemRepo;

    // Get farmers eligible for donation (farmers from whom consumer has received products)
    public ApiResponse<List<EligibleFarmerDTO>> getEligibleFarmersForDonation(String consumerEmail) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(consumerEmail);
            if (consumer == null) {
                return ApiResponse.error("Failed to get farmers", "Consumer not found");
            }

            // Query to get farmers from completed orders
            List<Object[]> results = orderItemRepo.findFarmersFromCompletedOrders(consumer.getConsumerId());

            List<EligibleFarmerDTO> eligibleFarmers = results.stream()
                    .map(result -> EligibleFarmerDTO.builder()
                            .farmerId((Integer) result[0])
                            .farmerName((String) result[1])
                            .farmerEmail((String) result[2])
                            .farmerRating((Double) result[3])
                            .completedOrdersCount(((Long) result[4]).intValue())
                            .totalPurchaseAmount((Double) result[5])
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success("Eligible farmers retrieved successfully", eligibleFarmers);

        } catch (Exception e) {
            logger.error("Failed to get eligible farmers: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve farmers", e.getMessage());
        }
    }

    // Updated create donation method with validation
    @Transactional
    public ApiResponse<DonationResponseDTO> createDonation(DonationRequestDTO request, String consumerEmail) {
        try {
            // Get consumer
            Consumer consumer = consumerRepo.findByConsumerEmail(consumerEmail);
            if (consumer == null) {
                return ApiResponse.error("Donation failed", "Consumer not found");
            }

            // Get farmer
            Farmer farmer = farmerRepo.findById(request.getFarmerId()).orElse(null);
            if (farmer == null) {
                return ApiResponse.error("Donation failed", "Farmer not found");
            }

            // VALIDATION: Check if consumer has completed orders from this farmer
            boolean hasCompletedOrders = orderItemRepo.existsCompletedOrderFromFarmer(
                    consumer.getConsumerId(), farmer.getFarmerId());

            if (!hasCompletedOrders) {
                return ApiResponse.error("Donation not allowed",
                        "You can only donate to farmers from whom you have received products");
            }

            // Create donation record
            Donation donation = new Donation();
            donation.setConsumer(consumer);
            donation.setFarmer(farmer);
            donation.setAmount(request.getAmount());
            donation.setMessage(request.getMessage());
            donation.setPaymentMethod(request.getPaymentMethod());
            donation.setStatus(DonationStatus.PENDING);

            // Save initial donation record
            donation = donationRepo.save(donation);

            // Process payment
            PaymentService.PaymentRequest paymentRequest = PaymentService.PaymentRequest.builder()
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .customerEmail(consumerEmail)
                    .description("Donation to " + farmer.getFirstName() + " " + farmer.getLastName())
                    .build();

            PaymentService.PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

            if (paymentResponse.isSuccess()) {
                donation.setStatus(DonationStatus.PROCESSING);
                donation.setTransactionId(paymentResponse.getTransactionId());
                donation = donationRepo.save(donation);

                DonationResponseDTO responseDTO = convertToResponseDTO(donation);
                responseDTO.setPaymentUrl(paymentResponse.getPaymentUrl());

                return ApiResponse.success("Donation initiated successfully", responseDTO);
            } else {
                donation.setStatus(DonationStatus.FAILED);
                donation.setFailureReason(paymentResponse.getMessage());
                donationRepo.save(donation);

                return ApiResponse.error("Donation failed", paymentResponse.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to create donation: {}", e.getMessage(), e);
            return ApiResponse.error("Donation failed", e.getMessage());
        }
    }
    // Add this method to DonationService
    public ApiResponse<DonationEligibilityDTO> checkDonationEligibility(
            String consumerEmail, int farmerId) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(consumerEmail);
            if (consumer == null) {
                return ApiResponse.error("Check failed", "Consumer not found");
            }

            boolean isEligible = orderItemRepo.existsCompletedOrderFromFarmer(
                    consumer.getConsumerId(), farmerId);

            DonationEligibilityDTO eligibility = new DonationEligibilityDTO();
            eligibility.setFarmerId(farmerId);
            eligibility.setEligible(isEligible);

            if (!isEligible) {
                eligibility.setReason("You need to complete at least one order from this farmer to donate");
            }

            return ApiResponse.success("Eligibility checked", eligibility);

        } catch (Exception e) {
            logger.error("Failed to check eligibility: {}", e.getMessage(), e);
            return ApiResponse.error("Check failed", e.getMessage());
        }
    }

    // Webhook endpoint to be called by payment gateway
    @Transactional
    public ApiResponse<Void> updatePaymentStatus(String transactionId, boolean success, String failureReason) {
        try {
            Donation donation = donationRepo.findByTransactionId(transactionId).orElse(null);
            if (donation == null) {
                return ApiResponse.error("Update failed", "Donation not found");
            }

            if (success) {
                donation.setStatus(DonationStatus.COMPLETED);
                donation.setCompletedAt(LocalDateTime.now());
            } else {
                donation.setStatus(DonationStatus.FAILED);
                donation.setFailureReason(failureReason);
            }

            donationRepo.save(donation);
            return ApiResponse.success("Payment status updated");

        } catch (Exception e) {
            logger.error("Failed to update payment status: {}", e.getMessage(), e);
            return ApiResponse.error("Update failed", e.getMessage());
        }
    }

    public ApiResponse<List<DonationResponseDTO>> getConsumerDonations(String consumerEmail) {
        try {
            Consumer consumer = consumerRepo.findByConsumerEmail(consumerEmail);
            if (consumer == null) {
                return ApiResponse.error("Failed to get donations", "Consumer not found");
            }

            List<Donation> donations = donationRepo.findByConsumer_ConsumerIdOrderByCreatedAtDesc(consumer.getConsumerId());
            List<DonationResponseDTO> dtos = donations.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Donations retrieved successfully", dtos);

        } catch (Exception e) {
            logger.error("Failed to get consumer donations: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve donations", e.getMessage());
        }
    }

    public ApiResponse<List<DonationResponseDTO>> getFarmerDonations(String farmerEmail) {
        try {
            Farmer farmer = farmerRepo.findByFarmerEmail(farmerEmail);
            if (farmer == null) {
                return ApiResponse.error("Failed to get donations", "Farmer not found");
            }

            List<Donation> donations = donationRepo.findByFarmer_FarmerIdOrderByCreatedAtDesc(farmer.getFarmerId());
            List<DonationResponseDTO> dtos = donations.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Donations retrieved successfully", dtos);

        } catch (Exception e) {
            logger.error("Failed to get farmer donations: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve donations", e.getMessage());
        }
    }

    public ApiResponse<DonationStatsDTO> getDonationStats(String userEmail, String userType) {
        try {
            DonationStatsDTO stats = new DonationStatsDTO();

            if ("CONSUMER".equals(userType)) {
                Consumer consumer = consumerRepo.findByConsumerEmail(userEmail);
                if (consumer == null) {
                    return ApiResponse.error("Failed to get stats", "Consumer not found");
                }

                Double totalDonated = donationRepo.getTotalDonationsByConsumerAndStatus(
                        consumer.getConsumerId(), DonationStatus.COMPLETED);
                stats.setTotalAmount(totalDonated != null ? totalDonated : 0.0);
                stats.setUserType("CONSUMER");

            } else if ("FARMER".equals(userType)) {
                Farmer farmer = farmerRepo.findByFarmerEmail(userEmail);
                if (farmer == null) {
                    return ApiResponse.error("Failed to get stats", "Farmer not found");
                }

                Double totalReceived = donationRepo.getTotalDonationsByFarmerAndStatus(
                        farmer.getFarmerId(), DonationStatus.COMPLETED);
                stats.setTotalAmount(totalReceived != null ? totalReceived : 0.0);
                stats.setUserType("FARMER");
            }

            return ApiResponse.success("Donation stats retrieved successfully", stats);

        } catch (Exception e) {
            logger.error("Failed to get donation stats: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve stats", e.getMessage());
        }
    }

    private DonationResponseDTO convertToResponseDTO(Donation donation) {
        return DonationResponseDTO.builder()
                .donationId(donation.getDonationId())
                .consumerName(donation.getConsumer().getConsumerFirstName() + " " +
                        donation.getConsumer().getConsumerLastName())
                .farmerName(donation.getFarmer().getFirstName() + " " +
                        donation.getFarmer().getLastName())
                .amount(donation.getAmount())
                .message(donation.getMessage())
                .status(donation.getStatus())
                .createdAt(donation.getCreatedAt())
                .completedAt(donation.getCompletedAt())
                .transactionId(donation.getTransactionId())
                .build();
    }
}
