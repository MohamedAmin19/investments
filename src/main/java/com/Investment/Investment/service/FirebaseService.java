package com.Investment.Investment.service;

import com.Investment.Investment.dto.InvestmentRequest;
import com.Investment.Investment.dto.InvestmentResponse;
import com.Investment.Investment.dto.PaginatedResponse;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "investments";
    private static final String INFLUENCERS_COLLECTION = "influencers";
    
    // Default influencer (CCG) - used when no referral code is provided
    private static final String DEFAULT_INFLUENCER = "CCG";
    
    // Predefined influencers with complex unique IDs
    // Map: uniqueId -> influencer name
    // Note: CCG has no ID - it's the default when no ref parameter is provided
    private static final Map<String, String> PREDEFINED_INFLUENCERS = new LinkedHashMap<>();
    
    static {
        PREDEFINED_INFLUENCERS.put("SH7X9K2M4PLQ", "Sherine hamdy");
        PREDEFINED_INFLUENCERS.put("HR3B8N5W2JKF", "Hasem rasmy");
        PREDEFINED_INFLUENCERS.put("FN6C4T9R1VXZ", "Farah nofal");
        PREDEFINED_INFLUENCERS.put("AT2Y7H3D8MNP", "Ahmed talaat");
        PREDEFINED_INFLUENCERS.put("KS5L9Q4G6BWC", "Khaled el sayed");
        PREDEFINED_INFLUENCERS.put("AR8F2K7J3XHT", "Ahmed rashad");
        // CCG has no ID - it's the default (no ref parameter needed)
        PREDEFINED_INFLUENCERS.put("PS4W6M9N1YRV", "POSH");
        PREDEFINED_INFLUENCERS.put("EX7Q3K8L2CTB", "EGX");
        PREDEFINED_INFLUENCERS.put("CL9P5H4D6ZJN", "COLLAB");
    }

    @Autowired
    public FirebaseService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Initialize influencers collection on application startup
     * Each influencer gets a unique ID, except CCG which is the default (no ID needed)
     */
    @PostConstruct
    public void initializeInfluencers() {
        try {
            // Create influencers with IDs
            for (Map.Entry<String, String> entry : PREDEFINED_INFLUENCERS.entrySet()) {
                String uniqueId = entry.getKey();
                String influencerName = entry.getValue();
                
                // Check if influencer with this ID already exists
                DocumentSnapshot doc = firestore.collection(INFLUENCERS_COLLECTION)
                        .document(uniqueId).get().get();
                
                if (!doc.exists()) {
                    // Create the influencer with the unique ID as document ID
                    Map<String, Object> influencerData = new HashMap<>();
                    influencerData.put("name", influencerName);
                    influencerData.put("uniqueId", uniqueId);
                    influencerData.put("createdAt", System.currentTimeMillis());
                    
                    firestore.collection(INFLUENCERS_COLLECTION).document(uniqueId).set(influencerData).get();
                    System.out.println("Created influencer: " + influencerName + " with ID: " + uniqueId);
                }
            }
            
            // Create CCG as the default influencer (no ID needed for URL)
            DocumentSnapshot ccgDoc = firestore.collection(INFLUENCERS_COLLECTION)
                    .document("DEFAULT_CCG").get().get();
            if (!ccgDoc.exists()) {
                Map<String, Object> ccgData = new HashMap<>();
                ccgData.put("name", DEFAULT_INFLUENCER);
                ccgData.put("uniqueId", null); // CCG has no referral ID
                ccgData.put("isDefault", true);
                ccgData.put("createdAt", System.currentTimeMillis());
                
                firestore.collection(INFLUENCERS_COLLECTION).document("DEFAULT_CCG").set(ccgData).get();
                System.out.println("Created default influencer: " + DEFAULT_INFLUENCER);
            }
        } catch (Exception e) {
            System.err.println("Error initializing influencers: " + e.getMessage());
        }
    }

    /**
     * Get influencer name by their unique ID
     */
    public String getInfluencerNameById(String uniqueId) {
        // First check in-memory map for faster lookup
        if (PREDEFINED_INFLUENCERS.containsKey(uniqueId.toUpperCase())) {
            return PREDEFINED_INFLUENCERS.get(uniqueId.toUpperCase());
        }
        
        // Fallback to Firestore lookup
        try {
            DocumentSnapshot doc = firestore.collection(INFLUENCERS_COLLECTION)
                    .document(uniqueId.toUpperCase()).get().get();
            
            if (doc.exists()) {
                return (String) doc.getData().get("name");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting influencer by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if an influencer ID is valid
     */
    public boolean isValidInfluencerId(String uniqueId) {
        return PREDEFINED_INFLUENCERS.containsKey(uniqueId.toUpperCase());
    }

    /**
     * Save investment with optional influencer ID from URL query parameter
     * If no influencer ID is provided, defaults to CCG
     * 
     * @param request The investment request data (form fields)
     * @param influencerId Optional influencer unique ID from URL (e.g., ?ref=SH7X9K2M4PLQ)
     * @return The ID of the saved investment
     */
    public String saveInvestment(InvestmentRequest request, String influencerId) {
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
            if (request.getCurrentInvestmentsOther() != null && !request.getCurrentInvestmentsOther().isEmpty()) {
                investmentData.put("currentInvestmentsOther", request.getCurrentInvestmentsOther());
            }
            investmentData.put("mostInterestedIn", request.getMostInterestedIn());
            investmentData.put("createdAt", System.currentTimeMillis());
            investmentData.put("updatedAt", System.currentTimeMillis());
            
            // Handle influencer ID from URL query parameter
            // Note: Invalid codes are rejected at controller level with 400 Bad Request
            if (influencerId != null && !influencerId.trim().isEmpty()) {
                String normalizedId = influencerId.trim().toUpperCase();
                
                // Look up the influencer name for this ID (already validated in controller)
                String influencerName = getInfluencerNameById(normalizedId);
                if (influencerName != null) {
                    investmentData.put("influencerId", normalizedId);
                    investmentData.put("referredBy", influencerName);
                }
            } else {
                // No ref parameter provided - default to CCG
                investmentData.put("referredBy", DEFAULT_INFLUENCER);
            }

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

    /**
     * Get paginated investments with optional filters
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param name Optional name filter (searches firstName, lastName, middleName)
     * @param influencer Optional influencer filter - can be:
     *                   - An influencer ID (e.g., SH7X9K2M4PLQ)
     *                   - "CCG" to filter by the default influencer
     */
    public PaginatedResponse<InvestmentResponse> getAllInvestmentsPaginated(int page, int size, String name, String influencer) {
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
                    boolean matchesFilter = true;
                    
                    // Apply name filter - search across firstName, lastName, and middleName
                    if (name != null && !name.trim().isEmpty()) {
                        String searchTerm = name.toLowerCase().trim();
                        String docFirstName = response.getFirstName() != null ? response.getFirstName().toLowerCase() : "";
                        String docLastName = response.getLastName() != null ? response.getLastName().toLowerCase() : "";
                        String docMiddleName = response.getMiddleName() != null ? response.getMiddleName().toLowerCase() : "";
                        
                        matchesFilter = docFirstName.contains(searchTerm) || 
                                       docLastName.contains(searchTerm) || 
                                       docMiddleName.contains(searchTerm);
                    }
                    
                    // Apply influencer filter
                    if (matchesFilter && influencer != null && !influencer.trim().isEmpty()) {
                        String filterValue = influencer.trim();
                        Map<String, Object> data = document.getData();
                        String docInfluencerId = data != null ? (String) data.get("influencerId") : null;
                        
                        // Check if filtering by "CCG" (the default influencer with no ID)
                        if (filterValue.equalsIgnoreCase("CCG")) {
                            // CCG registrations have no influencerId but referredBy = "CCG"
                            String docReferredBy = response.getReferredBy();
                            matchesFilter = "CCG".equalsIgnoreCase(docReferredBy) && docInfluencerId == null;
                        } else {
                            // Filter by influencer ID (stored in Firestore but not returned in response)
                            matchesFilter = docInfluencerId != null && docInfluencerId.equalsIgnoreCase(filterValue);
                        }
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
                    .currentInvestmentsOther((String) data.get("currentInvestmentsOther"))
                    .mostInterestedIn((String) data.get("mostInterestedIn"))
                    .createdAt((Long) data.get("createdAt"))
                    .updatedAt((Long) data.get("updatedAt"))
                    .referredBy((String) data.get("referredBy"))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error mapping investment data", e);
        }
    }
}
