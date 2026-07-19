//package com.sharkdom.util.stripe;
//
//import com.sharkdom.constants.stripe.StripePlanType;
//import org.springframework.stereotype.Service;
//
//@Service
//public class StripeUtils {
//
//    public Long getSeatAssignedByPlanType(StripePlanType planType) {
//        if (planType.equals(StripePlanType.STANDARD_MONTHLY_ONE) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_ONE) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_ONE) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_ONE) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_ONE) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_ONE) ||
//                planType.equals(StripePlanType.STANDARD_MONTHLY_ONE_US) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_ONE_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_ONE_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_ONE_US) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_ONE_US) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_ONE_US)) {
//            return 1L;
//        } else if (planType.equals(StripePlanType.STANDARD_MONTHLY_TWO) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_TWO) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_TWO) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_TWO) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_TWO) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_TWO) ||
//                planType.equals(StripePlanType.STANDARD_MONTHLY_TWO_US) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_TWO_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_TWO_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_TWO_US) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_TWO_US) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_TWO_US)) {
//            return 2L;
//        } else if (planType.equals(StripePlanType.STANDARD_MONTHLY_THREE) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_THREE) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_THREE) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_THREE) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_THREE) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_THREE) ||
//                planType.equals(StripePlanType.STANDARD_MONTHLY_THREE_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_THREE_US) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_THREE_US) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_THREE_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_THREE_US) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_THREE_US)) {
//            return 3L;
//        } else if (planType.equals(StripePlanType.STANDARD_MONTHLY_FOUR) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_FOUR) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_FOUR) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_FOUR) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_FOUR) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_FOUR) ||
//                planType.equals(StripePlanType.STANDARD_MONTHLY_FOUR_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_MONTHLY_FOUR_US) ||
//                planType.equals(StripePlanType.PREMIUM_MONTHLY_FOUR_US) ||
//                planType.equals(StripePlanType.STANDARD_YEARLY_FOUR_US) ||
//                planType.equals(StripePlanType.ENTERPRISE_YEARLY_FOUR_US) ||
//                planType.equals(StripePlanType.PREMIUM_YEARLY_FOUR_US)) {
//            return 4L;
//        } else {
//            return 0L;
//        }
//    }
//}