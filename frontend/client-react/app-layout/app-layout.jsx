import React from "react";
import {Outlet} from "react-router-dom";
import {
  ThemeProvider,
  createTheme,
  AppBar,
  Toolbar,
  Typography,
  CssBaseline,
  IconButton,
  Box,
  Tooltip,
} from "@mui/material";
import Brightness4Icon from "@mui/icons-material/Brightness4";
import Brightness7Icon from "@mui/icons-material/Brightness7";

const Layout = () => {
  const [mode, setMode] = React.useState("dark");

  const theme = React.useMemo(
    () => createTheme({"palette": {"mode": mode}}),
    [mode]);

  const onToggleMode = React.useCallback(
    () => setMode(mode === "light" ? "dark" : "light"),
    [mode, setMode]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline/>
      {/* Navigation bar. */}
      <AppBar position="static">
        <Toolbar>
          <Typography sx={{"mr": 2}}>
            LinkedPipes
          </Typography>
          <Box sx={{"flexGrow": 1, "display": "flex"}}>

          </Box>
          {/* Color mode toggle. */}
          <Box sx={{"flexGrow": 0}}>
            <Tooltip title="Toggle visual mode.">
              <IconButton sx={{ml: 1}} onClick={onToggleMode} color="inherit">
                {theme.palette.mode === "dark"
                  ? <Brightness7Icon/>
                  : <Brightness4Icon/>}
              </IconButton>
            </Tooltip>
          </Box>
        </Toolbar>
      </AppBar>
      {/* Container for the application. */}
      <main>
        <Outlet/>
      </main>
    </ThemeProvider>
  )
};

export default Layout;
