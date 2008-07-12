/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.URI;
import java.net.ConnectException;
import java.util.Properties;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

public class SocketConnectionFactory implements ConnectionFactory {

    private static Map<URI, SocketConnection> connections = new ConcurrentHashMap<URI, SocketConnection>();

    public void init(Properties props) {
    }

    public Connection getConnection(URI uri) throws java.io.IOException {

        SocketConnection conn = connections.get(uri);
        if (conn == null) {
            conn = new SocketConnection();
            conn.open(uri);
            SocketConnection old = connections.put(uri, conn);
            if (old != null) {
                try {
                    old.lock.lock();
                } catch (Exception e) {
                } finally {
                    try {
                        old.socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        try {
            conn.lock.tryLock(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Connection busy");
        }

        OutputStream ouputStream = conn.getOuputStream();
        ouputStream.write(30);
        ouputStream.flush();
        return conn;
    }

    class SocketConnection implements Connection {

        Socket socket = null;

        OutputStream socketOut = null;

        InputStream socketIn = null;

        private final Lock lock = new ReentrantLock();

        protected void open(URI uri) throws IOException {

            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            try {
                if (uri.getScheme().equalsIgnoreCase("ejbds")) {
                    SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(uri.getHost(), uri.getPort());
                    // use an anonymous cipher suite so that a KeyManager or
                    // TrustManager is not needed
                    // NOTE: this assumes that the cipher suite is known. A check
                    // -should- be done first.
                    final String[] enabledCipherSuites = {"SSL_DH_anon_WITH_RC4_128_MD5"};
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                    socket = sslSocket;
                } else {
                    socket = new Socket(uri.getHost(), uri.getPort());
                }

                socket.setTcpNoDelay(true);
            } catch (ConnectException e) {
                throw new ConnectException("Cannot connect to server '"+uri.toString()+"'.  Check that the server is started and that the specified serverURL is correct.");

            } catch (IOException e) {
                throw new IOException("Cannot connect to server: '"+uri.toString()+"'.  Exception: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (SecurityException e) {
                throw new IOException("Cannot access server: '"+uri.toString()+"' due to security restrictions in the current VM: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot  connect to server: '"+uri.toString()+"' due to an unkown exception in the OpenEJB client: " + e.getClass().getName() + " : " + e.getMessage());
            }

        }

        public void close() throws IOException {
            lock.unlock();
            try {
                if (socketOut != null)
                    socketOut.close();
                if (socketIn != null)
                    socketIn.close();
//                if (socket != null)
//                    socket.close();
            } catch (Throwable t) {
                throw new IOException("Error closing connection with server: " + t.getMessage());
            }
        }

        public InputStream getInputStream() throws IOException {
            /*----------------------------------*/
            /* Open input streams */
            /*----------------------------------*/
            try {
                socketIn = new Input(socket.getInputStream());
            } catch (StreamCorruptedException e) {
                throw new IOException("Cannot open input stream to server, the stream has been corrupted: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (IOException e) {
                throw new IOException("Cannot open input stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
            return socketIn;
        }

        public OutputStream getOuputStream() throws IOException {
            /*----------------------------------*/
            /* Openning output streams */
            /*----------------------------------*/
            try {
                socketOut = new Output(socket.getOutputStream());
            } catch (IOException e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
            return socketOut;
        }

    }

    public class Input extends java.io.FilterInputStream {

        public Input(InputStream in) {
            super(in);
        }

        public void close() throws IOException {
        }
    }

    public class Output extends java.io.FilterOutputStream {
        public Output(OutputStream out) {
            super(out);
        }

        public void close() throws IOException {
            flush();
        }
    }
}
