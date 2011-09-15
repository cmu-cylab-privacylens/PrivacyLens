<%@ include file="header.jsp" %>
<body>
	<div class="box">
	    <div class="box-header">
	        <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt=""/>
	    </div>
	    <div class="box-content">
		    <h1> <fmt:message key="title"/> </h1> 
		    <div id="tou-content">
	            ${tou.content}
		    </div>
	        <div id="tou-acceptance">
	            <form action="" method="post">
	                <div style="float:left;">
                        <input type="checkbox" id="accept" name="accept" value="true"/>
                        <label for="accept"><fmt:message key="accept"/></label>  
	                </div>
                    <div style="float:right;">
                        <button type="submit"><fmt:message key="confirm"/></button>
                    </div>
	            </form>
	        </div>
	    </div>
	</div>    
</body>
<%@ include file="footer.jsp" %>