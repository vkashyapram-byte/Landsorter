package com.govtech.landstack.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RemoteDocument(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("doc_type") val docType: String,
    @SerialName("file_path") val filePath: String,
    @SerialName("uploaded_by") val uploadedBy: String,
    @SerialName("verification_status") val verificationStatus: String,
    @SerialName("ocr_extracted_text") val ocrExtractedText: String? = null,
    @SerialName("ocr_processed_at") val ocrProcessedAt: String? = null,
    @SerialName("verified_by") val verifiedBy: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null,
    @SerialName("registration_id") val registrationId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteDispute(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("case_number") val caseNumber: String? = null,
    @SerialName("dispute_type") val disputeType: String,
    val description: String? = null,
    val source: String = "manual",
    @SerialName("source_reference") val sourceReference: String? = null,
    @SerialName("effective_date") val effectiveDate: String? = null,
    @SerialName("is_stay_order") val isStayOrder: Boolean = false,
    val status: String,
    @SerialName("parties_involved") val partiesInvolved: String,
    @SerialName("filed_by") val filedBy: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteRestriction(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("restriction_type") val restrictionType: String,
    val description: String? = null,
    val source: String,
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("effective_to") val effectiveTo: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteParcel(
    val ulpin: String,
    val state: String,
    val district: String,
    @SerialName("village_ward") val villageWard: String?,
    @SerialName("survey_number") val surveyNumber: String?,
    @SerialName("parcel_type") val parcelType: String?,
    @SerialName("area_sqm") val areaSqm: Double?,
    @SerialName("zoning_classification") val zoningClassification: String?,
    @SerialName("permissible_fsi") val permissibleFsi: Double?,
    @SerialName("permitted_use") val permittedUse: String?,
    @SerialName("parcel_data") val parcelData: kotlinx.serialization.json.JsonElement?,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteParcelOwner(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("owner_name") val ownerName: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("khata_number") val khataNumber: String? = null,
    @SerialName("right_type") val rightType: String? = null,
    @SerialName("ownership_share") val ownershipShare: Double? = null,
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("effective_to") val effectiveTo: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteBuildingPermission(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("sanction_number") val sanctionNumber: String? = null,
    @SerialName("approved_built_up_area") val approvedBuiltUpArea: Double? = null,
    val floors: Int? = null,
    val status: String? = null,
    @SerialName("submitted_by") val submittedBy: String? = null,
    @SerialName("decided_by") val decidedBy: String? = null,
    @SerialName("decided_at") val decidedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteParcelEncumbrance(
    val id: Long? = null,
    val ulpin: String,
    val lender: String? = null,
    @SerialName("loan_amount") val loanAmount: Double? = null,
    @SerialName("lien_status") val lienStatus: String? = null,
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_to") val validTo: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteParcelTaxRecord(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("tax_year") val taxYear: Int? = null,
    @SerialName("assessed_value") val assessedValue: Double? = null,
    @SerialName("annual_tax") val annualTax: Double? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    val arrears: Double? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteParcelRegistration(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("deed_number") val deedNumber: String? = null,
    @SerialName("registration_date") val registrationDate: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
    @SerialName("stamp_duty_paid") val stampDutyPaid: Double? = null,
    @SerialName("sub_office") val subOffice: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteDataConflict(
    val id: Long? = null,
    val ulpin: String,
    val category: String,
    val severity: String,
    val status: String,
    val details: String? = null,
    @SerialName("mismatched_values") val mismatchedValues: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RemoteApplication(
    val id: Long? = null,
    val ulpin: String,
    @SerialName("applicant_id") val applicantId: String,
    @SerialName("application_type") val applicationType: String,
    @SerialName("current_stage") val currentStage: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// RPC Request Models

@Serializable
data class RpcOwnershipMutationRequest(
    val p_ulpin: String,
    val new_owners: List<RemoteParcelOwner>
)

@Serializable
data class RpcApproveBuildingPermitRequest(
    val p_permit_id: Long,
    val p_decision: String
)

@Serializable
data class RpcResolveEncumbranceRequest(
    val p_encumbrance_id: Long
)

@Serializable
data class RpcMarkTaxPaidRequest(
    val p_tax_record_id: Long
)
