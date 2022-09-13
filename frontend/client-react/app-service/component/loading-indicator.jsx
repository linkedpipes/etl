import React from "react";
import {Box, CircularProgress} from "@mui/material";

const LoadingIndicator = () => {
  return (
    <Box sx={{"width": "40px", "mx": "auto"}}>
      <CircularProgress/>
    </Box>
  )
}

export default LoadingIndicator;
