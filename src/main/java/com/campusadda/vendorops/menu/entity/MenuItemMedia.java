package com.campusadda.vendorops.menu.entity;

import com.campusadda.vendorops.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_item_media")
public class MenuItemMedia extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType = "IMAGE";

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = Boolean.FALSE;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}