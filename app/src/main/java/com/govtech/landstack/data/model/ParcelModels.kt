package com.govtech.landstack.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Parcel(
    val base: BaseLayer,
    val essential: EssentialLayer,
    val additional: AdditionalLayer
)

@Serializable
data class BaseLayer(
    val ulpin: String,
    val state: String,
    val district: String,
    val taluk: String? = null,
    @SerialName("village_ward") val villageWard: String,
    @SerialName("survey_number") val surveyNumber: String,
    @SerialName("parcel_type") val parcelType: String,
    @SerialName("area_sqm") val areaSqm: Double,
    val geometry: Geometry
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

@Serializable
data class EssentialLayer(
    val ror: RecordOfRights,
    val registration: Registration,
    val zoning: Zoning,
    val planning: Planning? = null,
    val building: Building,
    val encumbrance: Encumbrance
)

@Serializable
data class Planning(
    @SerialName("commercial_use") val commercialUse: Boolean = false,
    @SerialName("industrial_use") val industrialUse: Boolean = false,
    @SerialName("road_reservation") val roadReservation: Boolean = false,
    @SerialName("development_zone") val developmentZone: String = "Unknown"
)

@Serializable
data class RecordOfRights(
    val owners: List<String>,
    @SerialName("khata_number") val khataNumber: String,
    @SerialName("right_type") val rightType: String,
    @SerialName("ownership_share") val ownershipShare: Double,
    @SerialName("mutation_history") val mutationHistory: List<String>
)

@Serializable
data class Registration(
    @SerialName("deed_number") val deedNumber: String,
    @SerialName("registration_date") val registrationDate: String,
    @SerialName("transaction_type") val transactionType: String,
    @SerialName("stamp_duty_paid") val stampDutyPaid: Double,
    @SerialName("sub_office") val subOffice: String
)

@Serializable
data class Zoning(
    val classification: String,
    @SerialName("permissible_fsi") val permissibleFsi: Double,
    @SerialName("permitted_use") val permittedUse: String
)

@Serializable
data class Building(
    @SerialName("sanction_number") val sanctionNumber: String,
    @SerialName("approved_built_up_area") val approvedBuiltUpArea: Double,
    val floors: Int,
    @SerialName("occupancy_status") val occupancyStatus: String
)

@Serializable
data class Encumbrance(
    val lender: String,
    @SerialName("loan_amount") val loanAmount: Double,
    @SerialName("lien_status") val lienStatus: String,
    @SerialName("validity_window") val validityWindow: String
)

@Serializable
data class AdditionalLayer(
    val utilities: Utilities,
    val taxation: Taxation,
    val valuation: Valuation,
    val environmental: Environmental
)

@Serializable
data class Utilities(
    @SerialName("water_connection_id") val waterConnectionId: String,
    @SerialName("electricity_connection_id") val electricityConnectionId: String,
    @SerialName("sewage_access") val sewageAccess: Boolean,
    @SerialName("road_access") val roadAccess: String
)

@Serializable
data class Taxation(
    @SerialName("assessed_value") val assessedValue: Double,
    @SerialName("annual_tax") val annualTax: Double,
    @SerialName("payment_status") val paymentStatus: String,
    val arrears: Double
)

@Serializable
data class Valuation(
    @SerialName("circle_rate") val circleRate: Double,
    @SerialName("market_value") val marketValue: Double
)

@Serializable
data class Environmental(
    @SerialName("flood_zone") val floodZone: Boolean,
    @SerialName("crz_flag") val crzFlag: Boolean,
    @SerialName("heritage_zone") val heritageZone: Boolean
)
