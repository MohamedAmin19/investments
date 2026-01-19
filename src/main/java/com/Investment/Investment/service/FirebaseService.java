package com.Investment.Investment.service;

import com.Investment.Investment.dto.InvestmentRequest;
import com.Investment.Investment.dto.InvestmentResponse;
import com.Investment.Investment.dto.PaginatedResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "investments";

    @Autowired
    public FirebaseService(Firestore firestore) {
        this.firestore = firestore;
    }

    public String saveInvestment(InvestmentRequest request) {
        try {
            CollectionReference investmentsRef = firestore.collection(COLLECTION_NAME);
            DocumentReference newInvestmentRef = investmentsRef.document();

            Map<String, Object> investmentData = new HashMap<>();
            investmentData.put("firstName", request.getFirstName());
            investmentData.put("middleName", request.getMiddleName());
            investmentData.put("lastName", request.getLastName());
            investmentData.put("age", request.getAge());
            investmentData.put("mobileNumber", request.getMobileNumber());
            investmentData.put("emailAddress", request.getEmailAddress());
            investmentData.put("profession", request.getProfession());
            if (request.getProfessionOther() != null && !request.getProfessionOther().isEmpty()) {
                investmentData.put("professionOther", request.getProfessionOther());
            }
            investmentData.put("currentInvestments", request.getCurrentInvestments());
            investmentData.put("mostInterestedIn", request.getMostInterestedIn());
            investmentData.put("createdAt", System.currentTimeMillis());
            investmentData.put("updatedAt", System.currentTimeMillis());

            // Save to Firestore
            newInvestmentRef.set(investmentData).get();
            
            return newInvestmentRef.getId();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error saving investment", e);
        } catch (Exception e) {
            throw new RuntimeException("Error saving investment", e);
        }
    }

    public List<InvestmentResponse> getAllInvestments() {
        try {
            CollectionReference investmentsRef = firestore.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> future = investmentsRef.get();
            QuerySnapshot snapshot = future.get();

            List<InvestmentResponse> investments = new ArrayList<>();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                InvestmentResponse response = mapToInvestmentResponse(document);
                if (response != null) {
                    investments.add(response);
                }
            }

            return investments;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching investments", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching investments", e);
        }
    }

    public PaginatedResponse<InvestmentResponse> getAllInvestmentsPaginated(int page, int size, String name) {
        try {
            CollectionReference investmentsRef = firestore.collection(COLLECTION_NAME);
            
            // Build query with filters
            Query query = investmentsRef.orderBy("createdAt", Query.Direction.DESCENDING);
            
            // Fetch all documents
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot snapshot = future.get();

            List<InvestmentResponse> allInvestments = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                InvestmentResponse response = mapToInvestmentResponse(document);
                if (response != null) {
                    // Apply name filter - search across firstName, lastName, and middleName
                    boolean matchesFilter = true;
                    
                    if (name != null && !name.trim().isEmpty()) {
                        String searchTerm = name.toLowerCase().trim();
                        String docFirstName = response.getFirstName() != null ? response.getFirstName().toLowerCase() : "";
                        String docLastName = response.getLastName() != null ? response.getLastName().toLowerCase() : "";
                        String docMiddleName = response.getMiddleName() != null ? response.getMiddleName().toLowerCase() : "";
                        
                        // Check if search term matches any of the three name fields
                        matchesFilter = docFirstName.contains(searchTerm) || 
                                       docLastName.contains(searchTerm) || 
                                       docMiddleName.contains(searchTerm);
                    }
                    
                    if (matchesFilter) {
                        allInvestments.add(response);
                    }
                }
            }

            // Get total count after filtering
            long totalElements = allInvestments.size();
            
            // Calculate pagination
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // Calculate pagination bounds
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, allInvestments.size());
            
            // Extract paginated subset
            List<InvestmentResponse> paginatedInvestments = new ArrayList<>();
            if (startIndex < allInvestments.size()) {
                paginatedInvestments = allInvestments.subList(startIndex, endIndex);
            }

            return PaginatedResponse.<InvestmentResponse>builder()
                    .data(paginatedInvestments)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching investments", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching investments", e);
        }
    }

    public InvestmentResponse getInvestmentById(String id) {
        try {
            DocumentReference investmentRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = investmentRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return mapToInvestmentResponse(document);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching investment", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching investment", e);
        }
    }

    public boolean deleteInvestment(String id) {
        try {
            DocumentReference investmentRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = investmentRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                investmentRef.delete().get();
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error deleting investment", e);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting investment", e);
        }
    }

    @SuppressWarnings("unchecked")
    private InvestmentResponse mapToInvestmentResponse(DocumentSnapshot document) {
        try {
            Map<String, Object> data = document.getData();
            if (data == null) {
                return null;
            }

            Object ageValue = data.get("age");
            String age = null;
            if (ageValue != null) {
                if (ageValue instanceof String) {
                    age = (String) ageValue;
                } else if (ageValue instanceof Long) {
                    age = String.valueOf(ageValue);
                } else if (ageValue instanceof Integer) {
                    age = String.valueOf(ageValue);
                } else {
                    age = ageValue.toString();
                }
            }

            Object currentInvestmentsValue = data.get("currentInvestments");
            List<String> currentInvestments = null;
            if (currentInvestmentsValue instanceof List) {
                currentInvestments = (List<String>) currentInvestmentsValue;
            }

            return InvestmentResponse.builder()
                    .id(document.getId())
                    .firstName((String) data.get("firstName"))
                    .middleName((String) data.get("middleName"))
                    .lastName((String) data.get("lastName"))
                    .age(age)
                    .mobileNumber((String) data.get("mobileNumber"))
                    .emailAddress((String) data.get("emailAddress"))
                    .profession((String) data.get("profession"))
                    .professionOther((String) data.get("professionOther"))
                    .currentInvestments(currentInvestments)
                    .mostInterestedIn((String) data.get("mostInterestedIn"))
                    .createdAt((Long) data.get("createdAt"))
                    .updatedAt((Long) data.get("updatedAt"))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error mapping investment data", e);
        }
    }
}

