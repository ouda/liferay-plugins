/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.sync.engine.service;

import com.liferay.sync.engine.model.ModelListener;
import com.liferay.sync.engine.model.SyncFile;
import com.liferay.sync.engine.model.SyncSite;
import com.liferay.sync.engine.service.persistence.SyncSitePersistence;
import com.liferay.sync.engine.util.FileUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.SQLException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shinn Lok
 */
public class SyncSiteService {

	public static SyncSite addSyncSite(
			long companyId, String filePathName, long groupId,
			long syncAccountId)
		throws Exception {

		// Sync site

		SyncSite syncSite = new SyncSite();

		syncSite.setCompanyId(companyId);
		syncSite.setFilePathName(filePathName);
		syncSite.setGroupId(groupId);
		syncSite.setSyncAccountId(syncAccountId);

		_syncSitePersistence.create(syncSite);

		// Sync file

		Files.createDirectories(Paths.get(filePathName));

		SyncFileService.addSyncFile(
			null, null, filePathName, FileUtil.getFileKey(filePathName),
			filePathName, null, filePathName, 0, groupId,
			syncSite.getSyncAccountId(), SyncFile.TYPE_FOLDER);

		return syncSite;
	}

	public static void deleteSyncSite(long syncSiteId) {
		try {
			_syncSitePersistence.deleteById(syncSiteId);
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}
		}
	}

	public static SyncSite fetchSyncSite(long syncSiteId) {
		try {
			return _syncSitePersistence.queryForId(syncSiteId);
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return null;
		}
	}

	public static SyncSite fetchSyncSite(long groupId, long syncAccountId) {
		try {
			return _syncSitePersistence.fetchByG_S(groupId, syncAccountId);
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return null;
		}
	}

	public static SyncSite fetchSyncSite(
		String filePathName, long syncAccountId) {

		try {
			return _syncSitePersistence.fetchByF_S(filePathName, syncAccountId);
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return null;
		}
	}

	public static List<SyncSite> findSyncSites(long syncAccountId) {
		try {
			return _syncSitePersistence.findBySyncAccountId(syncAccountId);
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return Collections.emptyList();
		}
	}

	public static List<Long> getActiveSyncSiteIds(long syncAccountId) {
		try {
			List<Long> activeSyncSiteIds = _activeSyncSiteIds.get(
				syncAccountId);

			if ((activeSyncSiteIds != null) && !activeSyncSiteIds.isEmpty()) {
				return activeSyncSiteIds;
			}

			activeSyncSiteIds = _syncSitePersistence.findByA_S(
				true, syncAccountId);

			_activeSyncSiteIds.put(syncAccountId, activeSyncSiteIds);

			return activeSyncSiteIds;
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return Collections.emptyList();
		}
	}

	public static SyncSitePersistence getSyncSitePersistence() {
		if (_syncSitePersistence != null) {
			return _syncSitePersistence;
		}

		try {
			_syncSitePersistence = new SyncSitePersistence();
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}
		}

		return _syncSitePersistence;
	}

	public static void registerModelListener(
		ModelListener<SyncSite> modelListener) {

		_syncSitePersistence.registerModelListener(modelListener);
	}

	public static void setActiveSyncSiteIds(
		long syncAccountId, List<Long> activeSyncSiteIds) {

		_activeSyncSiteIds.put(syncAccountId, activeSyncSiteIds);
	}

	public static void unregisterModelListener(
		ModelListener<SyncSite> modelListener) {

		_syncSitePersistence.unregisterModelListener(modelListener);
	}

	public static SyncSite update(SyncSite syncSite) {
		try {
			_syncSitePersistence.createOrUpdate(syncSite);

			return syncSite;
		}
		catch (SQLException sqle) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(sqle.getMessage(), sqle);
			}

			return null;
		}
	}

	private static Logger _logger = LoggerFactory.getLogger(
		SyncSiteService.class);

	private static Map<Long, List<Long>> _activeSyncSiteIds =
		new HashMap<Long, List<Long>>();
	private static SyncSitePersistence _syncSitePersistence =
		getSyncSitePersistence();

}