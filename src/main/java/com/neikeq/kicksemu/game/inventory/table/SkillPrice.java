package com.neikeq.kicksemu.game.inventory.table;

import com.neikeq.kicksemu.game.inventory.Expiration;
import com.neikeq.kicksemu.game.inventory.shop.Payment;
import com.neikeq.kicksemu.utils.table.Row;

public class SkillPrice {

    private int kash7;
    private int kash30;
    private int kashPerm;
    private int points7;
    private int points30;
    private int pointsPerm;

    public SkillPrice(Row row) {
        kash7 = Integer.valueOf(row.nextRow());
        kash30 = Integer.valueOf(row.nextRow());
        kashPerm = Integer.valueOf(row.nextRow());
        points7 = Integer.valueOf(row.nextRow());
        points30 = Integer.valueOf(row.nextRow());
        pointsPerm = Integer.valueOf(row.nextRow());
    }

    public int getPriceFor(Expiration expiration, Payment payment) {
        switch (expiration) {
            case DAYS_7:
                return payment == Payment.KASH ? kash7 : points7;
            case DAYS_30:
                return payment == Payment.KASH ? kash30 : points30;
            case DAYS_PERM:
                return payment == Payment.KASH ? kashPerm : pointsPerm;
            default:
                return -1;
        }
    }
}
