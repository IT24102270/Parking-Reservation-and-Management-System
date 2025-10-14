package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlotStrategyFactory {

    private final VipSlotStrategy vipSlotStrategy;
    private final GeneralSlotStrategy generalSlotStrategy;
    private final StaffSlotStrategy staffSlotStrategy;

    @Autowired
    public SlotStrategyFactory(VipSlotStrategy vipSlotStrategy,
                               GeneralSlotStrategy generalSlotStrategy,
                               StaffSlotStrategy staffSlotStrategy) {
        this.vipSlotStrategy = vipSlotStrategy;
        this.generalSlotStrategy = generalSlotStrategy;
        this.staffSlotStrategy = staffSlotStrategy;
    }

    /**
     * Returns the appropriate SlotValidationStrategy based on the slot's type.
     *
     * @param slot the Slot object to determine the type from
     * @return the corresponding strategy
     * @throws IllegalArgumentException if the type is not recognized
     */
    public SlotValidationStrategy getStrategy(Slot slot) {
        String type = slot.getType();
        switch (type != null ? type.toLowerCase() : "") {
            case "vip":
                return vipSlotStrategy;
            case "general":
                return generalSlotStrategy;
            case "staff":
                return staffSlotStrategy;
            default:
                throw new IllegalArgumentException("Unsupported slot type: " + type + ". Supported types: VIP, General, Staff.");
        }
    }
}
