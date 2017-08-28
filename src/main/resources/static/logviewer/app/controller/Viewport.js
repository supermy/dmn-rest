Ext.define('LogViewer.controller.Viewport', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.grid.column.Action'
    ],

    stores: [
        'Files'
    ],

    refs: [
        { ref: 'fileTree', selector: '#fileTree' },
        { ref: 'alertBox', selector: '#myalert'}
    ],

    init : function () {
        Ext.log('init viewport controller');
        this.getFilesStore().getProxy().on({
            exception: this.onProxyException,
            scope: this
        });
        this.control({
            'viewport #fileTree' : {
                itemclick : this.fileClick
            },
            'viewport tool[type="refresh"]' : {
                click: this.onRefresh
            },
            'viewport actioncolumn' : {
                click: this.handleActionColumn
            }
        });
    },

    fileClick : function(view, record, item, index, event) {
        Ext.log("in fileClick ");
        var logWin, fileName, cleanName, logUrl;

        if (record.isLeaf()) {
            fileName = record.get('filePath');
            logUrl = Ext.String.urlAppend('/logviewer/open', 'fileName='+fileName);
            cleanName = fileName.replace(/\W/g, '')
            Ext.log('open [' + logUrl + '] name [' + cleanName + ']');
            // window.location = logUrl;
            window.open(logUrl,'_blank');
            //是先到后台将要导出或者下载的数据 准备后，并将文件路径传到 前台，然后再指向该路径
            // Ext.Ajax.request({
            //     url:logUrl,
            //     success:function(res){
            //         var obj =Ext.decode(res.responseText);
            //         window.location.href =obj.path;
            //     }
            // });
        }
    },

    onRefresh : function() {
        Ext.log('in refresh');
        var fileTree = this.getFileTree();
        this.getAlertBox().hide();
        fileTree.getStore().load();
    },

    handleActionColumn : function(view, row, col, item, e, record) {
        var logWin, fileName, downloadUrl;
        Ext.log("handleActionColumn");
        if (record.isLeaf()) {
            fileName = record.get('filePath');
            this.onDownload(fileName);
        }
        return false;
    },

    onDownload : function(fileName) {
        Ext.log("Exporting to file:"+fileName);

        var frame, form, hidden, params, url;

        //frame = Ext.fly('exportframe').dom;
        //frame.src = Ext.SSL_SECURE_URL;

        form = Ext.fly('exportform').dom;

        //url = Ext.String.urlAppend('/logviewer/download', 'fileName='+fileName);
        //cleanName = fileName.replace(/\W/g, '')
        //Ext.log('download [' + url + '] name [' + cleanName + ']');
        //url = '../util/htmlresponse.json';
        //Ext.log(url)

        form.action = '/logviewer/download';

        //数据要么走 url 要么走 hidden
        hidden = document.getElementById('excelconfig');
        params = {fileName: fileName};
        hidden.value = Ext.encode(fileName);
        // Ext.log(fileName);

        // hidden.value = fileName;

        form.submit();
    },

    onProxyException : function(proxy, response, operation) {
        var alertBox = this.getAlertBox();
        alertBox.showError("An unexpected error occurred.");
    }
});