import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import CodiAllCategory from "./component/CodiAllCategory.tsx";
import SideMenu from "./component/SideMenu.tsx";
import {Box, Container, Divider} from "@mui/material";
import CodiOneBrand from "./component/CodiOneBrand.tsx";
import { ToastContainer } from 'react-toastify';
import CodiOneCategory from "./component/CodiOneCategory.tsx";
import BrandManager from "./component/BrandManager.tsx";
import ProductManager from "./component/ProductManager.tsx";

function App() {

  return (
      <BrowserRouter>
          <Box sx={{ display: 'flex', flexDirection: "row", gap: "8px" }}>
              <SideMenu/>
              <Divider orientation="vertical" flexItem />
              <Container sx={{padding: "8px"}}>
                  <Routes>
                      <Route path="/" element={<Navigate to="/all" />} />
                      <Route path="/all" element={<CodiAllCategory/>}/>
                      <Route path="/category" element={<CodiOneCategory/>}/>
                      <Route path="/brand" element={<CodiOneBrand/>}/>
                      <Route path="/product">
                          <Route path="brand" element={<BrandManager/>}/>
                          <Route path="product" element={<ProductManager/>}/>
                      </Route>
                      <Route path="*" element={<Navigate to="/all" />} />
                  </Routes>
              </Container>
              <ToastContainer/>
          </Box>
      </BrowserRouter>
  )
}

export default App
