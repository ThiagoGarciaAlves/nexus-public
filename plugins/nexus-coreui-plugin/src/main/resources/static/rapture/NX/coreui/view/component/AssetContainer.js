/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * Asset tabs container.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.component.AssetContainer', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-component-assetcontainer',
  requires: [
    'NX.Icons'
  ],

  /**
   * Currently shown asset model.
   *
   * @private
   */
  assetModel: undefined,

  /**
   * @override
   */
  initComponent: function() {
    Ext.apply(this, {
      autoScroll: true,

      layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
      },

      dockedItems: {
        xtype: 'nx-actions',

        items: [
          {
            xtype: 'button',
            text: NX.I18n.get('AssetInfo_Delete_Button'),
            glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
            action: 'deleteAsset',
            disabled: true
          }
        ]
      },

      items: [
        {
          xtype: 'nx-sorted-tabpanel',
          ui: 'nx-light',
          itemId: 'assetInfoTabs',
          activeTab: 0
        }
      ]
    });

    this.callParent();
  },

  /**
   * Shows an asset in container.
   *
   * @public
   * @param {NX.coreui.model.Asset} assetModel asset to be shown
   */
  refreshInfo: function (assetModel) {
    var me = this,
        iconName = 'asset-type-default',
        contentType;

    me.assetModel = assetModel;

    if (me.assetModel) {
      if (me.hidden) {
        me.show();
      }
      contentType = me.assetModel.get('contentType').replace('/', '-');
      if (NX.getApplication().getIconController().findIcon('asset-type-' + contentType, 'x16')) {
        iconName = 'asset-type-' + contentType;
      }
      me.setIconCls(NX.Icons.cls(iconName, 'x16'));
      me.setTitle(me.assetModel.get('name'));

      me.fireEvent('updated', me, me.assetModel);
    }
    else {
      me.hide();
    }
  },

  /**
   * Contribute a content tab related to the visible Asset. If the tabItemId already exists, it will be replaced.
   * @public
   */
  addTab: function (item) {
    var me = this, tabs = me.down('#assetInfoTabs');
    var existingTab = tabs.down('#' + item.itemId);
    if (existingTab) {
      tabs.remove(existingTab);
    }
    tabs.add(item);
    tabs.setActiveTab(0);
  }

});
