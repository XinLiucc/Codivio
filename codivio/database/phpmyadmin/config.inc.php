<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */

/**
 * phpMyAdmin configuration for Codivio project
 */

/* Set default charset */
$cfg['DefaultCharset'] = 'utf8mb4';
$cfg['DefaultCollation'] = 'utf8mb4_unicode_ci';

/* Force UTF-8 */
$cfg['ForceSSL'] = false;
$cfg['CharEditing'] = 'utf-8';
$cfg['DefaultLang'] = 'zh_CN';

/* MySQL connection charset */
$cfg['Servers'][$i]['connect_type'] = 'tcp';
$cfg['Servers'][$i]['compress'] = false;
$cfg['Servers'][$i]['AllowNoPassword'] = false;

/* Set session timezone */
$cfg['Servers'][$i]['SessionTimeZone'] = '+08:00';

/* Import/Export settings */
$cfg['Import']['charset'] = 'utf-8';
$cfg['Export']['charset'] = 'utf-8';
$cfg['Export']['file_template_table'] = '@TABLE@';
$cfg['Export']['file_template_database'] = '@DATABASE@';

/* Display settings */
$cfg['ShowAll'] = true;
$cfg['MaxRows'] = 30;
$cfg['Order'] = 'ASC';
$cfg['ProtectBinary'] = 'blob';
$cfg['ShowFunctionFields'] = true;
$cfg['ShowFieldTypesInDataEditView'] = true;
$cfg['InsertRows'] = 2;
$cfg['ForeignKeyDropdownOrder'] = 'content-id';
$cfg['ForeignKeyMaxLimit'] = 100;

/* Theme */
$cfg['ThemeDefault'] = 'pmahomme';
?>
