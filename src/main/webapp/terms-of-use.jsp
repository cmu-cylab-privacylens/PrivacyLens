<%@ include file="header.jsp" %>
<body>
	<div class="box">
	    <div style="float:right;">
	        <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt=""/>
	    </div>
	    <div style="float:left;">
		    <h1> <fmt:message key="title"/> </h1> 
		    <div id="tou-content">
	            ${tou.content}
		    </div>
	        <div id="tou-acceptance">
	            <form action="" method="post">
	                <div style="float:left;">
                        <input id="accept" type="checkbox" name="accept" value="true" />
                        <label for="accept"><fmt:message key="accept"/></label>  
	                </div>
                    <div style="float:right;">
                        <input type="submit" name="confirm" value="<fmt:message key="confirm"/>" />
                    </div>
                    <div style="clear:both;"></div>
	            </form>
	        </div>
	    </div>
	</div>    
</body>
<%@ include file="footer.jsp" %>