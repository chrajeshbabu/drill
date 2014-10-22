<#-- Licensed to the Apache Software Foundation (ASF) under one or more contributor
  license agreements. See the NOTICE file distributed with this work for additional
  information regarding copyright ownership. The ASF licenses this file to
  You under the Apache License, Version 2.0 (the "License"); you may not use
  this file except in compliance with the License. You may obtain a copy of
  the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License. -->

<#include "*/generic.ftl">
<#macro page_head>
</#macro>

<#macro page_body>
  <a href="/queries">back</a><br/>
  <div class="page-header">
  </div>
  <#if (model.getRunningQueries()?size > 0) >
    <h3>Running Queries</h3>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead>
           <td>Time</td>
           <td>Query Id</td>
           <td>Foreman</td>
        </thead>
        <tbody>
          <#list model.getRunningQueries() as query>
          <tr>
            <td>${query.getTime()}</td>
            <td>
              <a href="/running_profiles/${query.getQueryId()}">
                <div style="height:100%;width:100%">
                  ${query.getQueryId()}
                </div>
              </a>
            </td>
            <td>
              <a href="http://${query.getForeman()}:8047/running_profiles/${query.getQueryId()}" target="_blank">
                <div style="height:100%;width:100%">
                  ${query.getForeman()}
                </div>
              </a>
            </td>
          </tr>
          </#list>
        </tbody>
      </table>
    </div>
    <div class="page-header">
    </div>
  <#else>
    <div id="message" class="alert alert-info alert-dismissable">
      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
      <strong>No running queries.</strong>
    </div>
  </#if>
  <h3>Completed Queries</h3>
  <div class="table-responsive">
    <table class="table table-hover">
      <thead>
         <td>Time</td>
         <td>Query Id</td>
         <td>Foreman</td>
      </thead>
      <tbody>
        <#list model.getFinishedQueries() as query>
        <tr>
          <td>${query.getTime()}</td>
          <td>
            <a href="/profiles/${query.getQueryId()}">
              <div style="height:100%;width:100%">
                ${query.getQueryId()}
              </div>
            </a>
          </td>
          <td>
            <a href="http://${query.getForeman()}:8047/profiles/${query.getQueryId()}" target="_blank">
              <div style="height:100%;width:100%">
                ${query.getForeman()}
              </div>
            </a>
          </td>
        </tr>
        </#list>
      </tbody>
    </table>
  </div>
</#macro>

<@page_html/>