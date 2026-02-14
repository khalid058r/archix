package archix_base.organization.mapper;

import archix_base.organization.dto.InviteCodeDto;
import archix_base.organization.entity.InviteCode;

import java.time.LocalDateTime;

public class InviteCodeMapper {

    public static InviteCodeDto toDto(InviteCode entity) {
        if (entity == null)
            return null;

        InviteCodeDto dto = new InviteCodeDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setTeamId(entity.getTeam() != null ? entity.getTeam().getId() : null);
        dto.setTeamName(entity.getTeam() != null ? entity.getTeam().getName() : null);
        dto.setCreatedById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
        dto.setCreatedByName(entity.getCreatedBy() != null
                ? entity.getCreatedBy().getFirstName() + " " + entity.getCreatedBy().getLastName()
                : null);
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setMaxUses(entity.getMaxUses());
        dto.setUsedCount(entity.getUsedCount());
        dto.setActive(entity.isActive());
        dto.setExpired(entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now()));
        return dto;
    }
}
