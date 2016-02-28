/*
 * Copyright (C) 2005-2016 Christian P. Lerch <christian.p.lerch [at] gmail [dot] com>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kmworks.portal.rpc.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author cpl
 */
public class RPCDownloadInputStreamServlet extends HttpServlet {
  
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
    
    String[] path = request.getPathInfo().split("/");
    String token = path[path.length-1];
     
    File downloadFile = null;   // TODO: FileCacheUtil.findFile(token);
    
    if (downloadFile == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    response.setContentType("application/octet-stream");
    response.setContentLength((int) downloadFile.length());
    //response.setHeader( "Content-Disposition",
    //         String.format("attachment; filename=\"%s\"", downloadFile.getName()));

    OutputStream out = response.getOutputStream();
    try (FileInputStream in = new FileInputStream(downloadFile)) {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }
    out.flush();    
  }
}
