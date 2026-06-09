package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record LeaderProfile(
    String villageType,
    String leaderTypeId,
    String leadershipTitle,
    double powerBase,
    double wealthBase,
    double reputationBase,
    boolean canMobilize,
    boolean canDeclareWar,
    boolean canTradePolicy,
    List<String> questPool
) {
    public static final Codec<LeaderProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("village_type").forGetter(LeaderProfile::villageType),
        Codec.STRING.fieldOf("leader_type_id").forGetter(LeaderProfile::leaderTypeId),
        Codec.STRING.fieldOf("leadership_title").forGetter(LeaderProfile::leadershipTitle),
        Codec.DOUBLE.fieldOf("power_base").forGetter(LeaderProfile::powerBase),
        Codec.DOUBLE.fieldOf("wealth_base").forGetter(LeaderProfile::wealthBase),
        Codec.DOUBLE.fieldOf("reputation_base").forGetter(LeaderProfile::reputationBase),
        Codec.BOOL.fieldOf("can_mobilize").forGetter(LeaderProfile::canMobilize),
        Codec.BOOL.fieldOf("can_declare_war").forGetter(LeaderProfile::canDeclareWar),
        Codec.BOOL.fieldOf("can_trade_policy").forGetter(LeaderProfile::canTradePolicy),
        Codec.STRING.listOf().optionalFieldOf("quest_pool").forGetter(p -> Optional.ofNullable(p.questPool().isEmpty() ? null : p.questPool()))
    ).apply(instance, (vt, lt, tlt, pb, wb, rb, cm, cdw, ctp, qp) ->
        new LeaderProfile(vt, lt, tlt, pb, wb, rb, cm, cdw, ctp, qp.orElse(List.of()))
    ));
}