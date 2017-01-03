package cz.wincor.pnc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author matej.bludsky
 * 
 *         Class for soap ui operations with endpoints and binding and operations
 * 
 */

public class BindingTranslator {

    public static enum EndPointType {
        JBOSS, WAS;
    }

    public static class Operation {

        private String matcher;
        private String representation;

        public Operation(String matcher, String representation) {
            super();
            this.matcher = matcher;
            this.representation = representation;
        }

        public String getMatcher() {
            return matcher;
        }

        public void setMatcher(String matcher) {
            this.matcher = matcher;
        }

        public String getRepresentation() {
            return representation;
        }

        public void setRepresentation(String representation) {
            this.representation = representation;
        }

    }

    public static enum BindingType {
        /** DO NOT FORMAT */
        ACCOUNT_MOVEMENT("accountmovement", "AccountMovementServicePortBinding", new ArrayList<Operation>(), new HashMap<EndPointType,String>()), 
        ACCOUNT_OVERVIEW("accountoverview", "AccountMovementServicePortBinding", new ArrayList<Operation>(),  new HashMap<EndPointType,String>()), 
        CHECK_CASHING("checkcashing", "CheckCashingPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>() ), 
        CLIENT_SETTLEMENT("clientsettlement", "ClientSettlementServicePortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>()), 
        CORE("core", "CoreServicePortBinding", new ArrayList<Operation>(), new HashMap<EndPointType,String>()), 
        CUSTOMER_PREFERENCES("customerpreferences", "CustomerPreferencesPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>() ), 
        DEPOSIT("deposit", "DepositPortBinding", new ArrayList<Operation>(), new HashMap<EndPointType,String>()), 
        ERECEIPT("ereceipt", "EReceiptPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>()), 
        LIMIT_APPROVAL("limitapproval","LimitApprovalPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>() ), 
        PINCHANGE("pinchange", "PinChangeServicePortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>()), 
        REQUEST_SUPPORT("requestsupport", "RequestSupportPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>()), 
        SDM("sdm", "SdmPortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>()), 
        TRANSFER("transfer", "TransferServicePortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>() ), 
        WITHDRAWAL("withdrawal", "WithdrawalServicePortBinding", new ArrayList<Operation>(),new HashMap<EndPointType,String>() );

        static {

            ACCOUNT_MOVEMENT.getOperations().add(new Operation("AuthorizeAccountMovementRequest", "AuthorizeAccountMovement"));
            ACCOUNT_MOVEMENT.getOperations().add(new Operation("FinalizeAccountMovementRequest", "FinalizeAccountMovement"));

            ACCOUNT_MOVEMENT.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/AccountMovementServiceV02");
            ACCOUNT_MOVEMENT.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/AccountMovementServiceV02/PCEWsCcAccountMovement");
            
            ACCOUNT_OVERVIEW.getOperations().add(new Operation("AuthorizeAccountOverviewRequest", "AuthorizeAccountMovement"));
            ACCOUNT_OVERVIEW.getOperations().add(new Operation("FinalizeAccountOverviewRequest", "FinalizeAccountMovement"));

            ACCOUNT_OVERVIEW.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/AccountOverviewServiceV02");
            ACCOUNT_OVERVIEW.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/AccountOverviewServiceV02/PCEWsCcAccountOverview");
            
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingAddCheckRequest", "AddCheck"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingAuthorizeRequest", "Authorize"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingAuthorizeCashOutRequest", "AuthorizeCashOut"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingAuthorizeCheckDepositRequest", "AuthorizeCheckDeposit"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingAuthorizeNextCashOutRequest", "AuthorizeNextCashOut"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingFinalizeRequest", "Finalize"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingFinalizeCashOutRequest", "FinalizeCashOut"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingFinalizeCheckDepositRequest", "FinalizeCheckDeposit"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingFinalizeNextCashOutRequest", "FinalizeNextCashOut"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingFinishValidationRequest", "FinishValidation"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingInquireValidationStateRequest", "InquireValidationState"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingUpdateChecksRequest", "UpdateChecks"));
            CHECK_CASHING.getOperations().add(new Operation("CheckCashingValidateChecksRequest", "ValidateChecks"));

            
            CHECK_CASHING.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/CheckCashingServiceV02");
            CHECK_CASHING.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/CheckCashingServiceV02/PCEWsCcCheckCashingTransaction");
            
            CLIENT_SETTLEMENT.getOperations().add(new Operation("ProcessRequest", "ClientSettlement"));

            CLIENT_SETTLEMENT.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/ClientSettlementServiceV02");
            CLIENT_SETTLEMENT.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/ClientSettlementServiceV02/PCEWsCcClSettlem");
            
            CORE.getOperations().add(new Operation("CounterChangedRequest", "CounterChanged"));
            CORE.getOperations().add(new Operation("HeartBeatRequest", "HeartBeat"));
            CORE.getOperations().add(new Operation("InitWorkstationRequest", "InitWorkstation"));
            CORE.getOperations().add(new Operation("JournalInfoRequest", "JournalInfo"));
            CORE.getOperations().add(new Operation("LoginRequest", "Login"));
            CORE.getOperations().add(new Operation("LogoutRequest", "Logout"));
            CORE.getOperations().add(new Operation("EventRequest", "WorkstationEvent"));

            CORE.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/CoreServiceV02");
            CORE.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/CoreServiceV02/PCEWsCcCore");
            
            CUSTOMER_PREFERENCES.getOperations().add(new Operation("CustomerPreferencesLoadRequest", "LoadCustomerPreferences"));
            CUSTOMER_PREFERENCES.getOperations().add(new Operation("CustomerPreferencesSaveRequest", "SaveCustomerPreferences"));

            CUSTOMER_PREFERENCES.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/CustomerPreferencesServiceV02");
            CUSTOMER_PREFERENCES.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/CustomerPreferencesServiceV02/PCEWsCcCustomerPreferencesTransaction");
            
            DEPOSIT.getOperations().add(new Operation("DepositAddCashRequest", "AddCash"));
            DEPOSIT.getOperations().add(new Operation("DepositAddCheckRequest", "AddCheck"));
            DEPOSIT.getOperations().add(new Operation("DepositAuthorizeRequest", "Authorize"));
            DEPOSIT.getOperations().add(new Operation("DepositCancelDepositRequest", "CancelDeposit"));
            DEPOSIT.getOperations().add(new Operation("DepositFinalizeRequest", "Finalize"));
            DEPOSIT.getOperations().add(new Operation("DepositFinishCancellationRequest", "FinishCancellation"));
            DEPOSIT.getOperations().add(new Operation("DepositInquireCancellationStateRequest", "InquireCancellationState"));
            DEPOSIT.getOperations().add(new Operation("DepositFinishValidationRequest", "FinishValidation"));
            DEPOSIT.getOperations().add(new Operation("DepositInquireValidationStateRequest", "InquireValidationState"));
            DEPOSIT.getOperations().add(new Operation("DepositProcessClearingRequest", "ProcessClearing"));
            DEPOSIT.getOperations().add(new Operation("DepositUpdateChecksRequest", "UpdateChecks"));
            DEPOSIT.getOperations().add(new Operation("DepositValidateDepositRequest", "ValidateDeposit"));

            DEPOSIT.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/DepositServiceV02");
            DEPOSIT.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/DepositServiceV02/PCEWsCcDepositTransaction");
            
            ERECEIPT.getOperations().add(new Operation("SendEReceiptRequest", "SendEReceipt"));

            ERECEIPT.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/EReceiptServiceV02");
            ERECEIPT.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/EReceiptServiceV02/PCEWsCcSendEReceiptTransaction");
            
            LIMIT_APPROVAL.getOperations().add(new Operation("FinishApprovalRequest", "FinishApproval"));
            LIMIT_APPROVAL.getOperations().add(new Operation("InquireApprovalStateRequest", "InquireApprovalState"));
            LIMIT_APPROVAL.getOperations().add(new Operation("StartApprovalRequest", "StartApproval"));

            LIMIT_APPROVAL.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/LimitApprovalServiceV02");
            LIMIT_APPROVAL.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/LimitApprovalServiceV02/PCEWsCcLimitApprovalTransaction");
            
            PINCHANGE.getOperations().add(new Operation("AuthorizePinChangeRequest", "AuthorizePinChange"));
            PINCHANGE.getOperations().add(new Operation("FinalizePinChangeRequest", "FinalizePinChange"));

            PINCHANGE.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/PinChangeServiceV02");
            PINCHANGE.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/PinChangeServiceV02/PCEWsCcPinChange");
            
            REQUEST_SUPPORT.getOperations().add(new Operation("CloseRequestRequest", "CloseRequest"));
            REQUEST_SUPPORT.getOperations().add(new Operation("GetRequestStateRequest", "GetRequestState"));
            REQUEST_SUPPORT.getOperations().add(new Operation("RequestSupportRequest", "RequestSupport"));

            REQUEST_SUPPORT.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/RequestSupportServiceV02");
            REQUEST_SUPPORT.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/RequestSupportServiceV02/PCEWsCcRequestSupportTransaction");
            
            SDM.getOperations().add(new Operation("SdmDownloadRequest", "FinishSdmTask"));
            SDM.getOperations().add(new Operation("SdmProcessRequest", "InquirySdmTask"));

            SDM.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/SdmDownloadServiceV02");
            SDM.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/SdmServiceV02/PCEWsCcSdmTransaction");
            
            TRANSFER.getOperations().add(new Operation("AuthorizeTransferRequest", "AuthorizeTransfer"));
            TRANSFER.getOperations().add(new Operation("FinalizeTransferRequest", "FinalizeTransfer"));

            TRANSFER.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/TransferServiceV02");
            TRANSFER.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/TransferServiceV02/PCEWsCcTransfer");
            
            WITHDRAWAL.getOperations().add(new Operation("AuthorizeWithdrawalRequest", "AuthorizeWithdrawal"));
            WITHDRAWAL.getOperations().add(new Operation("FinalizeWithdrawalRequest", "FinalizeWithdrawal"));
            
            WITHDRAWAL.getEndpointSuffix().put(EndPointType.WAS, "/PCEWsClientConnector/WithdrawalServiceV02");
            WITHDRAWAL.getEndpointSuffix().put(EndPointType.JBOSS, "/PCEWsClientConnector/WithdrawalServiceV02/PCEWsCcWithdrawal");
            
        }

        private String matcher;
        private String binding;
        private List<Operation> operations;
        private Map<EndPointType,String> endpointSuffix;

        private BindingType(String matcher, String binding, List<Operation> operations, Map<EndPointType,String> endpointSuffix) {
            this.matcher = matcher;
            this.binding = binding;
            this.operations = operations;
            this.endpointSuffix = endpointSuffix;
        }

        public String getMatcher() {
            return matcher;
        }

        public void setMatcher(String matcher) {
            this.matcher = matcher;
        }

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public void setOperations(List<Operation> operations) {
            this.operations = operations;
        }

        public Map<EndPointType,String> getEndpointSuffix() {
            return endpointSuffix;
        }

        public void setEndpointSuffix(Map<EndPointType,String> endpointSuffix) {
            this.endpointSuffix = endpointSuffix;
        }

    };

    /**
     * determines the type of the request
     * 
     * @param message
     * @return
     */
    public static BindingType fromWSCCRequest(String message) {

        message = message.toLowerCase();

        if (message.contains(BindingType.ACCOUNT_MOVEMENT.getMatcher())) {
            return BindingType.ACCOUNT_MOVEMENT;
        } else if (message.contains(BindingType.ACCOUNT_OVERVIEW.getMatcher())) {
            return BindingType.ACCOUNT_OVERVIEW;
        } else if (message.contains(BindingType.CHECK_CASHING.getMatcher())) {
            return BindingType.CHECK_CASHING;
        } else if (message.contains(BindingType.CLIENT_SETTLEMENT.getMatcher())) {
            return BindingType.CLIENT_SETTLEMENT;
        } else if (message.contains(BindingType.CORE.getMatcher())) {
            return BindingType.CORE;
        } else if (message.contains(BindingType.CUSTOMER_PREFERENCES.getMatcher())) {
            return BindingType.CUSTOMER_PREFERENCES;
        } else if (message.contains(BindingType.DEPOSIT.getMatcher())) {
            return BindingType.DEPOSIT;
        } else if (message.contains(BindingType.ERECEIPT.getMatcher())) {
            return BindingType.ERECEIPT;
        } else if (message.contains(BindingType.LIMIT_APPROVAL.getMatcher())) {
            return BindingType.LIMIT_APPROVAL;
        } else if (message.contains(BindingType.PINCHANGE.getMatcher())) {
            return BindingType.PINCHANGE;
        } else if (message.contains(BindingType.REQUEST_SUPPORT.getMatcher())) {
            return BindingType.REQUEST_SUPPORT;
        } else if (message.contains(BindingType.SDM.getMatcher())) {
            return BindingType.SDM;
        } else if (message.contains(BindingType.TRANSFER.getMatcher())) {
            return BindingType.TRANSFER;
        } else if (message.contains(BindingType.WITHDRAWAL.getMatcher())) {
            return BindingType.WITHDRAWAL;
        }

        return null;
    }

    /**
     * Returns right operation type for given message
     * @param type
     * @param message
     * @return
     */
    public static String determineOperation(BindingType type, String message) {

        for (Iterator<Operation> iterator = type.getOperations().iterator(); iterator.hasNext();) {
            Operation operation = (Operation) iterator.next();
            if (message.toLowerCase().contains(operation.getMatcher().toLowerCase())) {
                return operation.getRepresentation();
            }
        }

        return null;
    }

}
